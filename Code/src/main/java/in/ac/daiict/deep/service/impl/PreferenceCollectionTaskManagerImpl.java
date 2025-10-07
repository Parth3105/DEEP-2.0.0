package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.service.PreferenceCollectionTaskManager;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class PreferenceCollectionTaskManagerImpl implements PreferenceCollectionTaskManager{
    private final Map<String, Map<Integer,ScheduledFuture<?>>> preferenceCollectionTasks;
    private final TaskScheduler taskScheduler;
    private final EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;

    @Lazy
    @Autowired
    public PreferenceCollectionTaskManagerImpl(TaskScheduler taskScheduler,EnrollmentPhaseDetailsService enrollmentPhaseDetailsService) {
        this.preferenceCollectionTasks = new ConcurrentHashMap<>();
        this.taskScheduler = taskScheduler;
        this.enrollmentPhaseDetailsService=enrollmentPhaseDetailsService;
    }

    public void scheduleCollection(String program, int semester, LocalDateTime endDateTime){
        Map<Integer,ScheduledFuture<?>> semesterBased=preferenceCollectionTasks.computeIfAbsent(program,k->new ConcurrentHashMap<>());
        ScheduledFuture<?> scheduledCollection=semesterBased.get(semester);
        if(scheduledCollection!=null){
            scheduledCollection.cancel(false);
        }

        Runnable closeWindow=() -> closeWindow(program,semester);
        ScheduledFuture<?> preferenceCollectionTask= taskScheduler.schedule(closeWindow, endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        semesterBased.put(semester,preferenceCollectionTask);
        preferenceCollectionTasks.put(program,semesterBased);
    }

    public void closeWindow(String program, int semester){
        Map<Integer,ScheduledFuture<?>> semesterBased=preferenceCollectionTasks.get(program);
        if(semesterBased==null) return;

        ScheduledFuture<?> scheduledCollection=semesterBased.get(semester);
        if(scheduledCollection!=null){
            scheduledCollection.cancel(false);
            try {
                ResponseDto responseDto = enrollmentPhaseDetailsService.autoCloseRegistration(program, semester);
                semesterBased.remove(semester);
                if(preferenceCollectionTasks.get(program).isEmpty()) preferenceCollectionTasks.remove(program);

                System.out.println("Preference Collection ended.");
            } catch (Exception e){
                log.error("Failed to close Preference-Collection Window due to error: {}",e.getMessage(),e);
                scheduleCollection(program, semester, LocalDateTime.now().plusDays(1));
            }
        }
    }
}
