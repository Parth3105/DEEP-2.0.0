package in.ac.daiict.deep.controller.admin;

import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.enums.CollectionWindowStateEnum;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.template.AdminTemplate;
import in.ac.daiict.deep.constant.template.FragmentTemplate;
import in.ac.daiict.deep.dto.AllocationReqFilterDto;
import in.ac.daiict.deep.entity.AllocationSummary;
import in.ac.daiict.deep.service.AllocationSummaryService;
import in.ac.daiict.deep.service.StudentService;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.util.allocation.AllocationSystem;
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
    private AllocationSystem allocationSystem;
    private StudentService studentService;
    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    private AllocationSummaryService allocationSummaryService;

    @GetMapping(AdminEndpoint.RUN_ALLOCATION_PAGE)
    public String renderRunAllocationPage(Model model){
        // Logic to send all enrollment-phase details containing all required statuses.
        CompletableFuture<Void> statusFetchFuture=CompletableFuture.supplyAsync(() -> enrollmentPhaseDetailsService.fetchAllEnrollmentPhaseDetails())
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

        // Sending List to thymeleaf to retrieve the filtered choices.
        model.addAttribute("executionFilter",new ArrayList<AllocationReqFilterDto>());

        return AdminTemplate.RUN_ALLOCATION_PAGE;
    }

    @GetMapping(AdminEndpoint.REFRESH_ALLOCATION_SUMMARY)
    public String refreshAllocationSummary(Model model){
        model.addAttribute("allocationSummary",allocationSummaryService.fetchAll());
        return FragmentTemplate.ALLOCATION_SUMMARY_FRAGMENT;
    }

    @PostMapping(AdminEndpoint.EXECUTE_ALLOCATION)
    public String initiateAllocation(@ModelAttribute("executionFilter") List<AllocationReqFilterDto> executionFilter, @RequestParam("registrationStatus") String registrationStatus, RedirectAttributes redirectAttributes){
        /*CompletableFuture.runAsync(() -> {
            if (registrationStatus.equalsIgnoreCase(CollectionWindowStateEnum.OPEN.toString()))
                enrollmentPhaseDetailsService.updateOnEndingPreferenceCollection();
        });*/

        List<CompletableFuture<Void>> futureMultipleAllocationTask=new ArrayList<>();
        for(AllocationReqFilterDto filter: executionFilter){
            futureMultipleAllocationTask.add(CompletableFuture.runAsync(() -> handleAllocation(filter.getProgram(),filter.getSemester(),redirectAttributes)));
        }

        try{
            CompletableFuture.allOf(futureMultipleAllocationTask.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException ce){
            log.error("Async task to handle allocation failed with error: {}", ce.getCause().getMessage(), ce.getCause());
            redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR));
        }
        return "redirect: "+AdminEndpoint.REFRESH_ALLOCATION_SUMMARY;
    }

    private void handleAllocation(String program, int semester, RedirectAttributes redirectAttributes){
        Map<String,Long> unmetReqCnt=new HashMap<>();
        CompletableFuture<ResponseDto> futureAllocationResponse=CompletableFuture.supplyAsync(() -> allocationSystem.initiateAllocation(program,semester,unmetReqCnt));
        CompletableFuture<Long> futureStudentCount=CompletableFuture.supplyAsync(() -> studentService.countStudentsByProgramAndSemester(program,semester));
        try {
            ResponseDto allocationResponse = futureAllocationResponse.join();
            long totalStudents = futureStudentCount.join();
            long allocatedCount = ResponseStatus.OK == allocationResponse.getStatus() ? totalStudents - unmetReqCnt.size() : 0;
            long unallocatedCount = totalStudents - allocatedCount;
            AllocationSummary allocationSummary =new AllocationSummary(program, semester, (int) allocatedCount, (int) unallocatedCount);
            allocationSummaryService.insertAllocationSummary(allocationSummary);
            redirectAttributes.addFlashAttribute("allocationStatus",allocationResponse);
        } catch (CompletionException ce){
            log.error("Async task to fetch status/summary data failed with error: {}", ce.getCause().getMessage(), ce.getCause());
            redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR));
        }
    }

/*
    @GetMapping(AdminEndpoint.RUN_ALLOCATION)
    public String renderRunAllocationPage(Model model){
        CompletableFuture<Void> statusFetchFuture=CompletableFuture.supplyAsync(() -> systemStatusService.fetchRegistrationStatus())
                .thenAccept(registrationStatus -> model.addAttribute("registrationStatus",registrationStatus));
        CompletableFuture<Void> allStatusFetchFuture=CompletableFuture.supplyAsync(() -> allocationStatusService.fetchAll())
                .thenAccept(allocationStatusDtoList -> model.addAttribute("allocationStatus",allocationStatusDtoList));
        CompletableFuture<Void> resultStatusFetchFuture=CompletableFuture.supplyAsync(() -> systemStatusService.fetchResultStatus())
                .thenAccept(resultStatus -> model.addAttribute("resultStatus",resultStatus));

        CompletableFuture.allOf(statusFetchFuture,allStatusFetchFuture,resultStatusFetchFuture).join();
        return AdminTemplate.RUN_ALLOCATION_PAGE;
    }
*/

/*
    @PostMapping(AdminEndpoint.EXECUTE_ALLOCATION)
    public String initiateAllocation(@PathVariable("semester") int semester, @RequestParam("registrationStatus") String registrationStatus, RedirectAttributes redirectAttributes){
        CompletableFuture.runAsync(() -> {
            if (registrationStatus.equalsIgnoreCase(CollectionWindowStateEnum.open.toString()))
                systemStatusService.updateOnClosingRegistration();
        });

        Map<String,Long> unmetReqCnt=new HashMap<>();
        ResponseDto allocationResponse=allocationSystem.initiateAllocation(semester,unmetReqCnt);

        // Send the successfully allocated count.
        long totalStudents=studentService.countAllStudents();
        long allocatedCount=ResponseStatus.OK==allocationResponse.getStatus()?totalStudents-unmetReqCnt.size():0;

        allocationStatusService.insertAllocationSummary(new AllocationStatus(semester,allocationResponse.getStatus(),(int)allocatedCount,unmetReqCnt.size()));

        redirectAttributes.addFlashAttribute("semester",semester);
        return "redirect:"+AdminEndpoint.RUN_ALLOCATION;
    }
*/
}
