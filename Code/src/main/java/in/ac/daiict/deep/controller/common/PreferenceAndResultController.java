package in.ac.daiict.deep.controller.common;

import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.endpoints.StudentEndpoint;
import in.ac.daiict.deep.constant.status.ResultStatusEnum;
import in.ac.daiict.deep.constant.template.AdminTemplate;
import in.ac.daiict.deep.constant.template.StudentTemplate;
import in.ac.daiict.deep.dto.*;
import in.ac.daiict.deep.entity.CoursePref;
import in.ac.daiict.deep.entity.SlotPref;
import in.ac.daiict.deep.security.auth.CustomUserDetails;
import in.ac.daiict.deep.service.*;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.util.dataloader.DataLoader;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
@AllArgsConstructor
@Slf4j
public class PreferenceAndResultController {
    private StudentService studentService;
    private StudentReqService studentReqService;
    private CoursePrefService coursePrefService;
    private SlotPrefService slotPrefService;
    private AllocationResultService allocationResultService;
    private SystemStatusService systemStatusService;
    private DataLoader dataLoader;

    @GetMapping(StudentEndpoint.PREFERENCE_SUMMARY)
    public String loadMyPreferenceSummary(Model model, RedirectAttributes redirectAttributes) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return fetchPreferenceSummary(userDetails.getUsername(), model, 'S', redirectAttributes);
    }

    @GetMapping(StudentEndpoint.ALLOCATION_RESULT)
    public String loadMyAllocationResult(Model model, RedirectAttributes redirectAttributes) {
        if(!systemStatusService.fetchResultStatus().equals(ResultStatusEnum.declared.toString())){
            redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.RESULTS_NOT_DECLARED));
            return "redirect:"+StudentEndpoint.HOME_PAGE;
        }
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return fetchAllocationResult(userDetails.getUsername(), model, 'S', redirectAttributes);
    }

    @GetMapping(AdminEndpoint.STUDENT_PREFERENCE_FILTER)
    public String loadSubmittedPreferences(@PathVariable("sid") String studentId, Model model, RedirectAttributes redirectAttributes) {
        return fetchPreferenceSummary(studentId, model, 'A', redirectAttributes);
    }
    @GetMapping(AdminEndpoint.DOWNLOAD_STUDENT_PREFERENCES)
    public void downloadStudentPreferences(HttpServletResponse httpServletResponse, @RequestParam("program") String program, @RequestParam("semester") int semester) {
        ByteArrayOutputStream byteArrayOutputStream = dataLoader.createStudentPrefSheet(program, semester);
        try {
            if (byteArrayOutputStream==null) {
                httpServletResponse.setStatus(ResponseStatus.NOT_FOUND);
                httpServletResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                httpServletResponse.setHeader("Pragma", "no-cache");
                httpServletResponse.setDateHeader("Expires", 0);
                httpServletResponse.setContentType("application/json");
                httpServletResponse.getOutputStream().write(ResponseMessage.STUDENT_PREFERENCES_NOT_FOUND.getBytes());
            }
            else {
                String downloadFilename = "Student Preferences.csv";
                httpServletResponse.setContentType("text/csv");
                httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"");
                httpServletResponse.getOutputStream().write(byteArrayOutputStream.toByteArray());
                httpServletResponse.getOutputStream().flush();
            }
        } catch (IOException ioe) {
            log.error("I/O operation to download file failed: {}", ioe.getMessage(), ioe);
            httpServletResponse.setStatus(ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(AdminEndpoint.ALLOCATION_RESULTS_FILTER)
    public String loadAllocationResult(@PathVariable("sid") String studentId, Model model, RedirectAttributes redirectAttributes) {
        return fetchAllocationResult(studentId, model, 'A', redirectAttributes);
    }


    private String fetchPreferenceSummary(String studentId, Model model, char requester, RedirectAttributes redirectAttributes) {
        // Fetch the semester & program of the student.
        StudentDto student = studentService.fetchStudentDto(studentId);
        if (student == null) {
            // not found student.
            if (requester == 'S')
                redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
            else
                redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.STUDENT_NOT_FOUND));
            if (requester == 'S') return "redirect:" + StudentEndpoint.HOME_PAGE;
            return "redirect:" + AdminEndpoint.STUDENT_PREFERENCE;
        }

        // Fetch the student requirements of the student.
        CompletableFuture<List<StudentReqDto>> fetchingStudentReq = CompletableFuture.supplyAsync(() -> studentReqService.fetchStudentRequirements(studentId));

        // Fetch the course preferences slot-wise.
        CompletableFuture<List<CoursePrefDto>> fetchingCoursePref = CompletableFuture.supplyAsync(() -> coursePrefService.fetchStudentCoursePref(studentId));

        // Fetch the slot preferences.
        CompletableFuture<List<SlotPrefDto>> fetchingSlotPref = CompletableFuture.supplyAsync(() -> slotPrefService.fetchStudentSlotPref(studentId));

        CompletableFuture.allOf(fetchingStudentReq, fetchingCoursePref, fetchingSlotPref).join();
        List<StudentReqDto> studentReqDtoList;
        List<CoursePrefDto> coursePrefDtoList;
        List<SlotPrefDto> slotPrefDtoList;
        try {
            studentReqDtoList = fetchingStudentReq.get();
            coursePrefDtoList= fetchingCoursePref.get();
            slotPrefDtoList= fetchingSlotPref.get();
        } catch (InterruptedException | ExecutionException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting for preference data", e);
            }
            else log.error("Async task to fetch preference data failed with error: {}", e.getCause().getMessage(), e.getCause());

            redirectAttributes.addFlashAttribute("internalServerError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
            if (requester == 'S') return "redirect:" + StudentEndpoint.HOME_PAGE;
            return "redirect:" + AdminEndpoint.STUDENT_PREFERENCE;
        }
        if (studentReqDtoList == null || coursePrefDtoList == null || slotPrefDtoList == null) {
            // not found preferences.
            if (requester == 'S')
                redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.USER_NOT_REGISTERED));
            else
                redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.STUDENT_NOT_REGISTERED));
            if (requester == 'S') return "redirect:" + StudentEndpoint.HOME_PAGE;
            return "redirect:" + AdminEndpoint.STUDENT_PREFERENCE;
        }

        // Send the preference summary details.
        model.addAttribute("studentInfo", student);
        model.addAttribute("studentRequirements", studentReqDtoList);
        model.addAttribute("coursePreferences", coursePrefDtoList);
        model.addAttribute("slotPreferences", slotPrefDtoList);

        if (requester == 'S') return StudentTemplate.PREFERENCE_SUMMARY_PAGE;
        else return AdminTemplate.STUDENTS_PREFERENCES_PAGE;
    }

    private String fetchAllocationResult(String studentId, Model model, char requester, RedirectAttributes redirectAttributes) {
        StudentDto studentDto = studentService.fetchStudentDto(studentId);
        if (studentDto == null) {
            // not found student.
            if (requester == 'S')
                redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
            else
                redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.STUDENT_NOT_FOUND));
            if (requester == 'S') return "redirect:" + StudentEndpoint.HOME_PAGE;
            else return "redirect:" + AdminEndpoint.ALLOCATION_RESULTS;
        }

        CompletableFuture<List<AllocationResultDto>> fetchingAllocationResult =CompletableFuture.supplyAsync(() -> allocationResultService.fetchAllocationResult(studentId, studentDto.getProgram()));
        CompletableFuture<String> fetchingResultStatus =CompletableFuture.supplyAsync(() -> systemStatusService.fetchResultStatus());

        CompletableFuture.allOf(fetchingAllocationResult, fetchingResultStatus);
        List<AllocationResultDto> allocationResultDtoList;
        String resultStatus=null;
        try {
            allocationResultDtoList = fetchingAllocationResult.get();
        } catch (InterruptedException | ExecutionException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting for allocation results", e);
            }
            else log.error("Async task to fetch allocation result failed with error: {}", e.getCause().getMessage(), e.getCause());

            redirectAttributes.addFlashAttribute("internalServerError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
            if (requester == 'S') return "redirect:" + StudentEndpoint.HOME_PAGE;
            return "redirect:" + AdminEndpoint.STUDENT_PREFERENCE;
        }

        try {
            resultStatus= fetchingResultStatus.get();
        } catch (InterruptedException | ExecutionException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting for result status", e);
            }
            else log.error("Async task to fetch result status failed with error: {}", e.getCause().getMessage(), e.getCause());

            if(requester=='S'){
                redirectAttributes.addFlashAttribute("internalServerError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
                return "redirect:" + StudentEndpoint.HOME_PAGE;
            }
        }
        if (allocationResultDtoList == null || (requester=='S' && (resultStatus==null || resultStatus.equals(ResultStatusEnum.pending.toString())))) {
            // not found any results.
            if(requester=='S'){
                redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.RESULTS_NOT_FOUND_STUDENT));
                return "redirect:" + StudentEndpoint.HOME_PAGE;
            }
            else{
                redirectAttributes.addFlashAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.RESULTS_NOT_FOUND_ADMIN));
                return "redirect:"+AdminEndpoint.ALLOCATION_RESULTS;
            }
        }

        // send allocation result details
        model.addAttribute("studentInfo", studentDto);
        model.addAttribute("allocationResult", allocationResultDtoList);
        if (requester == 'S') return StudentTemplate.ALLOCATION_RESULT_PAGE;
        else return AdminTemplate.ALLOCATION_RESULTS_PAGE;
    }
}
