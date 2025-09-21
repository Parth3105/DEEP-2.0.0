package in.ac.daiict.deep.unit.service;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.service.impl.PreferenceCollectionTaskManagerImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class PreferenceCollectionTaskManagerTest {

    @Mock private PreferenceCollectionTaskManagerImpl preferenceCollectionTaskManager;
    @Mock private TaskScheduler taskScheduler;
    @Mock private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;

    @Test
    public void test_autoCloseFunctionality() throws InterruptedException {
//        when(enrollmentPhaseDetailsService.autoCloseRegistration(anyString(),anyInt())).thenReturn(new ResponseDto(ResponseStatus.OK, ResponseMessage.SUCCESS));
        preferenceCollectionTaskManager.scheduleCollection("ICT",6, LocalDateTime.now().plusSeconds(1));
        Thread.sleep(5000);
    }
}
