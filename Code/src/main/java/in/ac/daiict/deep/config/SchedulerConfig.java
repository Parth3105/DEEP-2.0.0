package in.ac.daiict.deep.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executors;

@Configuration
public class SchedulerConfig {
/*
    @Bean
    public TaskScheduler taskScheduler(){
        return new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());
    }
*/

    @Bean
    public TaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler=new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("PreferenceCollectionWindowScheduler-");
        taskScheduler.initialize();
        return taskScheduler;
    }
}
