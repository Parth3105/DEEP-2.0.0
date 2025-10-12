package in.ac.daiict.deep.controller.admin;

import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.template.FragmentTemplate;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.service.AllocationSummaryService;
import in.ac.daiict.deep.service.CourseService;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.service.StudentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Controller
@AllArgsConstructor
public class DataRefreshController {

    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    private StudentService studentService;
    private CourseService courseService;
    private AllocationSummaryService allocationSummaryService;

    @PostMapping(AdminEndpoint.REFRESH_ENROLLMENT_PHASE_DETAILS)
    public String refreshEnrollmentPhaseDetails(){
        enrollmentPhaseDetailsService.updateEnrollmentPhaseDetails();
        return FragmentTemplate.ENROLLMENT_PHASE_DETAILS;
    }

    @GetMapping(AdminEndpoint.REFRESH_UPLOAD_STATUS)
    public String refreshUploadStatus(Model model){
        CompletableFuture<Void> futureUploadStatusDtoList=CompletableFuture.supplyAsync(() -> studentService.fetchStudentDataUploadStatus())
                .thenAccept(uploadStatusDtoList ->  model.addAttribute("studentCountTable",uploadStatusDtoList));

        CompletableFuture<Void> futureCourseCnt=CompletableFuture.supplyAsync(() -> courseService.fetchCourseCnt())
                .thenAccept(courseCnt -> model.addAttribute("courseCount",courseCnt));

        try{
            CompletableFuture.allOf(futureUploadStatusDtoList,futureCourseCnt).join();
        }
        catch (CompletionException ce){
            log.error("Async task to upload all data failed with error: {}", ce.getCause().getMessage(), ce.getCause());
            model.addAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR));
        }
        return FragmentTemplate.UPLOAD_STATUS_FRAGMENT;
    }

    @GetMapping(AdminEndpoint.REFRESH_ALLOCATION_SUMMARY)
    public String refreshAllocationSummary(Model model){
        model.addAttribute("allocationSummary",allocationSummaryService.fetchAll());
        return FragmentTemplate.ALLOCATION_SUMMARY_FRAGMENT;
    }

    @GetMapping(AdminEndpoint.REFRESH_TERM_DETAILS)
    public String refreshTermData(Model model){
        model.addAttribute("downloadTermDetails",enrollmentPhaseDetailsService.fetchAllProgramAndSemester());
        return FragmentTemplate.DOWNLOAD_TERM_DATA_FRAGMENT;
    }
}
