package in.ac.daiict.deep.controller.admin;

import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.enums.CollectionWindowStateEnum;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.template.FragmentTemplate;
import in.ac.daiict.deep.dto.EnrollmentPhaseDetailsDto;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.service.AllocationSummaryService;
import in.ac.daiict.deep.service.CourseOfferingService;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.service.InstituteReqService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Controller
@AllArgsConstructor
public class DashboardController {

    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    private InstituteReqService instituteReqService;
    private CourseOfferingService courseOfferingService;
    private AllocationSummaryService allocationSummaryService;

    @PostMapping(AdminEndpoint.BEGIN_COLLECTION)
    public String startPreferenceCollection(@RequestParam("program") String program, @RequestParam("semester") int semester, @RequestParam("close-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closeDate, Model model){
        // Ensure that institute-requirements and seat-matrix of the same are uploaded.
        CompletableFuture<Boolean> isRequirementPresentTask=CompletableFuture.supplyAsync(() -> instituteReqService.isRequirementPresent(program,semester));
        CompletableFuture<Boolean> isOfferPresentTask=CompletableFuture.supplyAsync(() -> courseOfferingService.isOfferPresent(program, semester));
        try {
            CompletableFuture.allOf(isRequirementPresentTask, isOfferPresentTask).join();

            boolean isRequirementPresent = isRequirementPresentTask.get();
            boolean isOfferPresent = isOfferPresentTask.get();
            if (!isRequirementPresent || !isOfferPresent) {
                List<String> missingDataMsg = new ArrayList<>();
                if (!isRequirementPresent)
                    missingDataMsg.add("Institute Requirements for program: " + program + " and semester: " + semester + " seems to be missing. Ensure that the required data has been uploaded.");
                if (!isOfferPresent)
                    missingDataMsg.add("Seat Matrix for program: " + program + " and semester: " + semester + " seems to be missing. Ensure that the required data has been uploaded.");
                model.addAttribute("missingData", new ResponseDto(ResponseStatus.BAD_REQUEST, missingDataMsg));
                return FragmentTemplate.TOAST_MESSAGE_DETAILS;
            }
        } catch (ExecutionException | InterruptedException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while verifying the existence of entries related to program:"+program+" & semester:"+semester+" in institute-requirements and seat-matrix.", e);
            }
            else log.error("Async task to verify the existence of data failed with error: {}", e.getCause().getMessage(), e.getCause());
            model.addAttribute("internalServerError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
            return FragmentTemplate.TOAST_MESSAGE_DETAILS;
        }
        ResponseDto responseDto = enrollmentPhaseDetailsService.updateOnStartingPreferenceCollection(program, semester, closeDate);
        if(responseDto.getStatus()==ResponseStatus.OK) responseDto=new ResponseDto(ResponseStatus.OK, "Preference collection period has begun for "+program+" Sem-"+semester+".");
        model.addAttribute("preferenceCollectionWindowStatus", responseDto);
        return FragmentTemplate.TOAST_MESSAGE_DETAILS;
    }

    @PostMapping(AdminEndpoint.EXTEND_COLLECTION_PERIOD)
    public String extendCollectionPeriod(@RequestParam("program") String program, @RequestParam("semester") int semester, @RequestParam("close-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closeDate, Model model){
        System.out.println("Reached");

        if(!enrollmentPhaseDetailsService.fetchCollectionWindowState(program,semester).equalsIgnoreCase(CollectionWindowStateEnum.OPEN.toString())){
            model.addAttribute("notExtendable",new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.CANNOT_EXTEND_PERIOD));
            return FragmentTemplate.TOAST_MESSAGE_DETAILS;
        }
        ResponseDto responseDto = enrollmentPhaseDetailsService.updateOnStartingPreferenceCollection(program, semester, closeDate);
        if(responseDto.getStatus()==ResponseStatus.OK) responseDto=new ResponseDto(ResponseStatus.OK, "Preference collection period has been extended "+program+" Sem-"+semester+".");
        model.addAttribute("preferenceCollectionWindowStatus", responseDto);
        return FragmentTemplate.TOAST_MESSAGE_DETAILS;
    }

    @PostMapping(AdminEndpoint.END_COLLECTION)
    public String endCollection(@RequestParam("program") String program, @RequestParam("semester") int semester, Model model){
        System.out.println(program);
        System.out.println(semester);
        enrollmentPhaseDetailsService.updateOnEndingPreferenceCollection(program,semester);
        return FragmentTemplate.TOAST_MESSAGE_DETAILS;
    }

    @PostMapping(AdminEndpoint.DECLARE_RESULTS)
    public String declareResults(@RequestParam("program") String program, @RequestParam("semester") int semester, Model model){
        CompletableFuture<String> collectionWindowStateFuture = CompletableFuture.supplyAsync(() -> enrollmentPhaseDetailsService.fetchCollectionWindowState(program, semester));
        CompletableFuture<Boolean> allocationStateFuture = CompletableFuture.supplyAsync(() -> allocationSummaryService.checkIfExists(program, semester));

        String collectionWindowState = collectionWindowStateFuture.join();
        boolean allocationState = allocationStateFuture.join();

        ResponseDto responseDto;
        // If collection-window is closed and allocation is complete then only allow to declare results.
        if(collectionWindowState.equals(CollectionWindowStateEnum.CLOSED.toString()) && allocationState) responseDto=enrollmentPhaseDetailsService.updateOnDeclaringResults(program,semester);
        else if(!allocationState) responseDto=new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.RESULT_DECLARATION_FORBIDDEN_BEFORE_ALLOCATION);
        else responseDto=new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.RESULT_DECLARATION_FORBIDDEN_ON_WINDOW_OPEN);

        model.addAttribute("preferenceCollectionWindowStatus",responseDto);
        return FragmentTemplate.TOAST_MESSAGE_DETAILS;
    }

    @PostMapping(AdminEndpoint.REFRESH_ENROLLMENT_PHASE_DETAILS)
    public String refreshEnrollmentPhaseDetails(){
        enrollmentPhaseDetailsService.updateEnrollmentPhaseDetails();
        return FragmentTemplate.ENROLLMENT_PHASE_DETAILS;
    }

    @GetMapping(AdminEndpoint.FETCH_ENROLLMENT_DETAILS)
    public String fetchEnrollmentPhaseDetails(Model model){
        List<EnrollmentPhaseDetailsDto> enrollmentPhaseDetailsDtoList=enrollmentPhaseDetailsService.fetchDashboardDetails();
        if(enrollmentPhaseDetailsDtoList==null) model.addAttribute("internalServerError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
        else model.addAttribute("enrollmentPhaseDetails",enrollmentPhaseDetailsDtoList);
        return FragmentTemplate.ENROLLMENT_PHASE_DETAILS;
    }
}