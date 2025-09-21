package in.ac.daiict.deep.controller.admin;

import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.template.FragmentTemplate;
import in.ac.daiict.deep.dto.EnrollmentPhaseDetailsDto;
import in.ac.daiict.deep.dto.ResponseDto;
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
                    missingDataMsg.add("Institute Requirements for program:" + program + " and semester:" + semester + " seems to be missing. Ensure that the required data has been uploaded.");
                if (!isOfferPresent)
                    missingDataMsg.add("Seat Matrix for program:" + program + " and semester:" + semester + " seems to be missing. Ensure that the required data has been uploaded.");
                model.addAttribute("missingData", new ResponseDto(ResponseStatus.BAD_REQUEST, missingDataMsg));
                return "";
            }
        } catch (ExecutionException | InterruptedException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while verifying the existence of entries related to program:"+program+" & semester:"+semester+" in institute-requirements and seat-matrix.", e);
            }
            else log.error("Async task to verify the existence of data failed with error: {}", e.getCause().getMessage(), e.getCause());
            model.addAttribute("internalServerError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
            return "";
        }

        ResponseDto responseDto=enrollmentPhaseDetailsService.updateOnStartingPreferenceCollection(program,semester,closeDate);
        model.addAttribute("preferenceCollectionWindowStatus",responseDto);
        return "";
    }

    @PostMapping(AdminEndpoint.EXTEND_COLLECTION_PERIOD)
    public String extendCollectionPeriod(@RequestParam("program") String program, @RequestParam("semester") int semester, @RequestParam("close-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closeDate, Model model){
        ResponseDto responseDto=enrollmentPhaseDetailsService.updateOnExtendingCollectionPeriod(program,semester,closeDate);
        model.addAttribute("preferenceCollectionWindowStatus",responseDto);
        return "";
    }

    @PostMapping(AdminEndpoint.END_COLLECTION)
    public String endCollection(@RequestParam("program") String program, @RequestParam("semester") int semester, Model model){
        ResponseDto responseDto=enrollmentPhaseDetailsService.updateOnEndingPreferenceCollection(program,semester);
        model.addAttribute("preferenceCollectionWindowStatus",responseDto);
        return "";
    }

    @PostMapping(AdminEndpoint.DECLARE_RESULTS)
    public String declareResults(@RequestParam("program") String program, @RequestParam("semester") int semester, Model model){
        ResponseDto responseDto=enrollmentPhaseDetailsService.updateOnDeclaringResults(program,semester);
        model.addAttribute("preferenceCollectionWindowStatus",responseDto);
        return "";
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