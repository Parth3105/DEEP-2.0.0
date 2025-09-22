package in.ac.daiict.deep.init;

import in.ac.daiict.deep.constant.enums.CollectionWindowStateEnum;
import in.ac.daiict.deep.entity.EnrollmentPhaseDetails;
import in.ac.daiict.deep.service.PreferenceCollectionTaskManager;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@AllArgsConstructor
public class PreferenceCollectionJobBootstrap implements ApplicationRunner {
    EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    PreferenceCollectionTaskManager preferenceCollectionTaskManager;
    @Override
    public void run(ApplicationArguments args) {
        List<EnrollmentPhaseDetails> enrollmentPhaseDetailsList=enrollmentPhaseDetailsService.fetchDetailsWithOpenCollectionWindow();
        enrollmentPhaseDetailsList.forEach(enrollmentPhaseDetails -> {
            if(!enrollmentPhaseDetails.getEndDate().isBefore(LocalDate.now())) preferenceCollectionTaskManager.scheduleCollection(enrollmentPhaseDetails.getProgram(),enrollmentPhaseDetails.getSemester(),enrollmentPhaseDetails.getEndDate().atTime(23,59));
            else enrollmentPhaseDetailsService.autoCloseRegistration(enrollmentPhaseDetails.getProgram(),enrollmentPhaseDetails.getSemester());
        });

/*
        String regStatus= enrollmentPhaseDetailsService.fetchRegistrationStatus();
        if(regStatus.equalsIgnoreCase(CollectionWindowStateEnum.OPEN.toString())) {
            RegistrationCloseDate registrationCloseDate= enrollmentPhaseDetailsService.fetchRegistrationCloseDate();
            if(registrationCloseDate.getCloseDate().isAfter(LocalDate.now())){
                preferenceCollectionTaskManager.updateCloseRegistrationDate(registrationCloseDate.getCloseDate());
                preferenceCollectionTaskManager.startRegistration();
            }
            else enrollmentPhaseDetailsService.updateOnClosingRegistration();
        }
*/
    }
}