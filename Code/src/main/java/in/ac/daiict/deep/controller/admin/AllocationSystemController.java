package in.ac.daiict.deep.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.enums.ResultStateEnum;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.template.AdminTemplate;
import in.ac.daiict.deep.constant.template.FragmentTemplate;
import in.ac.daiict.deep.service.AllocationSummaryService;
import in.ac.daiict.deep.service.StudentService;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.util.allocation.AllocationTaskHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Controller
@AllArgsConstructor
public class AllocationSystemController {
    private AllocationTaskHandler allocationTaskHandler;
    private StudentService studentService;
    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    private AllocationSummaryService allocationSummaryService;

    @GetMapping(AdminEndpoint.RUN_ALLOCATION_PAGE)
    public String renderRunAllocationPage(Model model){
        // Logic to send all enrollment-phase details containing all required statuses.
        CompletableFuture<Void> statusFetchFuture=CompletableFuture.supplyAsync(() -> enrollmentPhaseDetailsService.fetchEnrollmentPhaseDetailsByResultState(ResultStateEnum.PENDING.toString()))
                .thenAccept(registrationStatus -> model.addAttribute("allRequiredStatus",registrationStatus));

/*
        // Logic to send the available program & semester.
        CompletableFuture<Void> futureFetchProgramAndSemDetails= CompletableFuture.supplyAsync(() -> studentService.fetchDistinctProgramAndSemester())
                .thenAccept(studentDtoList -> model.addAttribute("ProgramAndSem",studentDtoList));

        // Logic to send allocation summary
        CompletableFuture<Void> allStatusFetchFuture=CompletableFuture.supplyAsync(() -> allocationSummaryService.fetchAll())
                .thenAccept(allocationStatusDtoList -> model.addAttribute("allocationStatus",allocationStatusDtoList));

        // Logic to send result declaration status for thymeleaf to manipulate execution button.
        CompletableFuture<Void> resultStatusFetchFuture=CompletableFuture.supplyAsync(() -> enrollmentPhaseDetailsService.fetchResultStatus())
                .thenAccept(resultStatus -> model.addAttribute("resultStatus",resultStatus));
*/

        try{
            statusFetchFuture.join();
        } catch (CompletionException ce){
            log.error("Async task to fetch status/summary data failed with error: {}", ce.getCause().getMessage(), ce.getCause());
            model.addAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR));
        }

        return AdminTemplate.RUN_ALLOCATION_PAGE;
    }

    @GetMapping(AdminEndpoint.REFRESH_ALLOCATION_SUMMARY)
    public String refreshAllocationSummary(Model model){
        model.addAttribute("allocationSummary",allocationSummaryService.fetchAll());
        return FragmentTemplate.ALLOCATION_SUMMARY_FRAGMENT;
    }

    @PostMapping(AdminEndpoint.EXECUTE_ALLOCATION)
    public String initiateAllocation(@RequestParam("executionFilter") String executionFilter, RedirectAttributes redirectAttributes, Model model){

//      Parsing JSON containing the filters for executing allocation.
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String,List<String>> programSemesterMapping;
        try {
            programSemesterMapping=objectMapper.readValue(executionFilter,new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            log.error("JSON processing to read program-semester filters to execute allocation failed with error: {}",e.getMessage());
            model.addAttribute("jsonParsingError",new ResponseDto(ResponseStatus.BAD_REQUEST, ResponseMessage.JSON_PARSING_ERROR_ALLOCATION_EXECUTION));
            return FragmentTemplate.TOAST_MESSAGE_DETAILS;
        }

        // Formatting parsed JSON and converting semester in integer format.
        List<String> semesterParsingErrorMsg= new ArrayList<>();
        Map<String, List<Integer>> allocationFilter=new HashMap<>();
        for(Map.Entry<String,List<String>> programSemesterEntry: programSemesterMapping.entrySet()){
            for(String semester: programSemesterEntry.getValue()){
                if(!semester.matches("\\d+$")) semesterParsingErrorMsg.add(ResponseMessage.SEMESTER_PARSING_ERROR+semester+" for program: "+programSemesterEntry.getKey());
                else{
                    List<Integer> semesters=allocationFilter.getOrDefault(programSemesterEntry.getKey(),new ArrayList<>());
                    semesters.add(Integer.parseInt(semester));
                    allocationFilter.put(programSemesterEntry.getKey(),semesters);
                }
            }
        }

        // Passing semester parsing error if any.
        redirectAttributes.addFlashAttribute("semesterParsingError",new ResponseDto(ResponseStatus.BAD_REQUEST,semesterParsingErrorMsg));

        // Initiating allocation using allocationTaskHandler.
        List<ResponseDto> allocationTaskStatus=allocationTaskHandler.initiateAllocationTasks(allocationFilter);
        redirectAttributes.addFlashAttribute("allocationStatus",allocationTaskStatus);

        return "redirect:"+AdminEndpoint.RUN_ALLOCATION_PAGE;
    }
}
