package in.ac.daiict.deep.controller.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.endpoints.StudentEndpoint;
import in.ac.daiict.deep.constant.enums.CollectionWindowStateEnum;
import in.ac.daiict.deep.constant.template.StudentTemplate;
import in.ac.daiict.deep.entity.CoursePref;
import in.ac.daiict.deep.entity.SlotPref;
import in.ac.daiict.deep.entity.Student;
import in.ac.daiict.deep.entity.StudentReq;
import in.ac.daiict.deep.security.auth.CustomUserDetails;
import in.ac.daiict.deep.service.*;
import in.ac.daiict.deep.dto.ResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Controller
@AllArgsConstructor
public class EnrollmentController {
    private StudentService studentService;
    private CourseService courseService;
    private InstituteReqService instituteReqService;
    private StudentReqService studentReqService;
    private CoursePrefService coursePrefService;
    private SlotPrefService slotPrefService;
    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;

    private Validator validator;

    @GetMapping(StudentEndpoint.PREFERENCE_FORM)
    public String renderEnrollmentForm(Model model, RedirectAttributes redirectAttributes) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Send the semester & program of students and institute requirements.
        Student student = studentService.fetchStudentData(userDetails.getUsername());
        if (student == null) {
            // not found student.
            model.addAttribute("renderResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
            return "redirect:" + StudentEndpoint.HOME_PAGE;
        }
        else if (!enrollmentPhaseDetailsService.fetchCollectionWindowState(student.getProgram(),student.getSemester()).equalsIgnoreCase(CollectionWindowStateEnum.OPEN.toString())) {
            return "redirect:" + StudentEndpoint.HOME_PAGE;
        }
        else if (student.isHasEnrolled()) return "redirect:" + StudentEndpoint.PREFERENCE_SUMMARY;

        model.addAttribute("semester", student.getSemester());
        model.addAttribute("program", student.getProgram());

        CompletableFuture<Void> fetchingInstituteReq = CompletableFuture.supplyAsync(() -> instituteReqService.findInstituteReq(student.getProgram(), student.getSemester()))
                .thenAccept(instituteReqDtoList -> model.addAttribute("instituteRequirements", instituteReqDtoList));

        // Send the available courses to Student with required information
        CompletableFuture<Void> fetchingAvailableCourses = CompletableFuture.supplyAsync(() -> courseService.fetchAvailableCourses(student.getProgram(), student.getSemester()))
                .thenAccept(availableCourseDtoList -> model.addAttribute("availableCourses", availableCourseDtoList));

        try {
            CompletableFuture.allOf(fetchingInstituteReq, fetchingAvailableCourses).join();
        } catch (CompletionException ce) {
            log.error("Async task to fetch institute-requirements/available-courses failed with error: {}", ce.getCause().getMessage(), ce.getCause());

            redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR));
            return "redirect:" + StudentEndpoint.HOME_PAGE;
        }
        return StudentTemplate.ENROLLMENT_FORM_PAGE;
    }

    @PostMapping(StudentEndpoint.SUBMIT_PREFERENCE)
    public String loadSubmittedPreferences(@RequestParam("program") String program, @RequestParam("semester") int semester, @RequestParam("studentRequirements") String studentRequirements, @RequestParam("coursePreferences") String coursePreferences, @RequestParam("slotPreferences") String slotPreferences, RedirectAttributes redirectAttributes) {
        if(studentRequirements ==null || coursePreferences ==null || slotPreferences ==null){
            redirectAttributes.addFlashAttribute("preferenceMissing",new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.PREFERENCE_MISSING));
            return "redirect:" + StudentEndpoint.HOME_PAGE;
        }
        if (!enrollmentPhaseDetailsService.fetchCollectionWindowState(program, semester).equalsIgnoreCase(CollectionWindowStateEnum.OPEN.toString())) {
            redirectAttributes.addFlashAttribute("preferenceSubmissionResponse", new ResponseDto(ResponseStatus.FORBIDDEN, ResponseMessage.LATE_SUBMISSION));
            return "redirect:" + StudentEndpoint.HOME_PAGE;
        }
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String studentId = userDetails.getUsername();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> categoryReqMapping;
        Map<String,List<String>> slotCoursePrefMap;
        List<String> selectedSlotPreferences;
        try {
            categoryReqMapping = objectMapper.readValue(studentRequirements, new TypeReference<>(){});
            slotCoursePrefMap=objectMapper.readValue(coursePreferences,new TypeReference<>(){});
            selectedSlotPreferences=objectMapper.readValue(slotPreferences,new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            log.error("JSON processing to read student enrollment-details failed with error: {}",e.getMessage());
            redirectAttributes.addFlashAttribute("jsonParsingError",new ResponseDto(ResponseStatus.BAD_REQUEST, ResponseMessage.JSON_PARSING_ERROR_FORM_SUBMISSION));
            return "redirect:" + StudentEndpoint.HOME_PAGE;
        }

        // Variable to track any constraint violation in case of student-requirements, course-preference and slot-preferences.
        AtomicReference<String> violationMsg=new AtomicReference<>(null);

        // record Student Requirements.
        Map<String, String> finalCategoryReqMapping = categoryReqMapping;
        CompletableFuture<Void> recordingStudentReqs = CompletableFuture.runAsync(() -> {
            List<StudentReq> studentReqs = new ArrayList<>();

            // Variable to check if any constraint of course-preference violated.
            boolean isConstraintViolated=false;

            for (Map.Entry<String, String> entry : finalCategoryReqMapping.entrySet()) {
                StudentReq studentReq=new StudentReq(studentId, entry.getKey(), Integer.parseInt(entry.getValue()));

                // Validate received Student-requirement.
                Set<ConstraintViolation<StudentReq>> violations=validator.validate(studentReq);
                if(!violations.isEmpty()){
                    for(ConstraintViolation<StudentReq> violation: violations) violationMsg.set(violation.getMessage());
                    isConstraintViolated=true;
                    break;
                }
                studentReqs.add(studentReq);
            }
            if(!isConstraintViolated) studentReqService.insertAll(studentReqs);
        });

        // record Course Preferences.
        Map<String, List<String>> finalSlotCoursePrefMap = slotCoursePrefMap;
        CompletableFuture<Void> recordingCoursePrefs = CompletableFuture.runAsync(() -> {
            List<CoursePref> coursePrefs = new ArrayList<>();

            // Variable to check if any constraint of course-preference violated.
            boolean isConstraintViolated=false;

            for(Map.Entry<String,List<String>> entry: finalSlotCoursePrefMap.entrySet()){
                String slot=entry.getKey();
                List<String> courseList=entry.getValue();

                for(int pref=0;pref<courseList.size();pref++){
                    CoursePref coursePref=new CoursePref(studentId, slot, pref + 1, courseList.get(pref));

                    // Validate received Student-requirement.
                    Set<ConstraintViolation<CoursePref>> violations=validator.validate(coursePref);
                    if(!violations.isEmpty()) {
                        for (ConstraintViolation<CoursePref> violation : violations) violationMsg.set(violation.getMessage());
                        isConstraintViolated=true;
                        break;
                    }
                    coursePrefs.add(coursePref);
                }

                if(isConstraintViolated) break;
            }
            if(!isConstraintViolated) coursePrefService.insertAll(coursePrefs);
        });

        // record Slot Preferences
        List<String> finalSelectedSlotPreferences = selectedSlotPreferences;
        CompletableFuture<Void> recordingSlotPref = CompletableFuture.runAsync(() -> {
            List<SlotPref> slotPrefs = new ArrayList<>();

            // Variable to check if any constraint of course-preference violated.
            boolean isConstraintViolated=false;

            for(int pref = 0; pref < finalSelectedSlotPreferences.size(); pref++){
                SlotPref slotPref=new SlotPref(studentId, pref + 1, finalSelectedSlotPreferences.get(pref));

                // Validate received Student-requirement.
                Set<ConstraintViolation<SlotPref>> violations=validator.validate(slotPref);
                if(!violations.isEmpty()){
                    for(ConstraintViolation<SlotPref> violation: violations) violationMsg.set(violation.getMessage());
                    isConstraintViolated=true;
                    break;
                }
                slotPrefs.add(slotPref);
            }
            if(!isConstraintViolated) slotPrefService.insertAll(slotPrefs);
        });

        try {
            CompletableFuture.allOf(recordingStudentReqs, recordingCoursePrefs, recordingSlotPref).join();

            // Action on constraint violation of student-requirement.
            if(violationMsg.get()!=null){
                redirectAttributes.addFlashAttribute("badRequest", new ResponseDto(ResponseStatus.BAD_REQUEST,violationMsg.get()));
                return "redirect:" + StudentEndpoint.PREFERENCE_FORM;
            }

            studentService.updateEnrollmentStatus(studentId);
        } catch (CompletionException ce) {
            log.error("Async task to record student enrollment-details failed with error: {}", ce.getCause().getMessage(), ce.getCause());

            redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR));
            return "redirect:" + StudentEndpoint.HOME_PAGE;
        }
        return "redirect:" + StudentEndpoint.PREFERENCE_SUMMARY;
    }
}
