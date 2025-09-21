package in.ac.daiict.deep.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PreferenceCollectionTaskManager {
    void scheduleCollection(String program, int semester, LocalDateTime endDateTime);
    void closeWindow(String program, int semester);
}
