package in.ac.daiict.deep.init;

import in.ac.daiict.deep.constant.enums.CollectionWindowStateEnum;
import in.ac.daiict.deep.service.PreferenceCollectionTaskManager;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.util.status.RegistrationCloseDate;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class PreferenceCollectionJobBootstrap implements ApplicationRunner {
    EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    PreferenceCollectionTaskManager preferenceCollectionTaskManager;
    @Override
    public void run(ApplicationArguments args) {
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