package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.endpoints.StudentEndpoint;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.entity.CoursePref;
import in.ac.daiict.deep.entity.SlotPref;
import in.ac.daiict.deep.entity.StudentReq;
import in.ac.daiict.deep.service.CoursePrefService;
import in.ac.daiict.deep.service.PreferenceCollectionService;
import in.ac.daiict.deep.service.SlotPrefService;
import in.ac.daiict.deep.service.StudentReqService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
@Slf4j
public class PreferenceCollectionServiceImpl implements PreferenceCollectionService {

    private StudentReqService studentReqService;
    private CoursePrefService coursePrefService;
    private SlotPrefService slotPrefService;

    @Override
    @Transactional
    public void recordPreferenceDetails(String sid, List<StudentReq> studentReqList, List<CoursePref> coursePrefList, List<SlotPref> slotPrefList) {
        try {
            studentReqService.deleteBySid(sid);
            studentReqService.insertAll(studentReqList);

            coursePrefService.deleteBySid(sid);
            coursePrefService.insertAll(coursePrefList);

            slotPrefService.deleteBySid(sid);
            slotPrefService.insertAll(slotPrefList);
        } catch (Exception e){
            throw new RuntimeException("Failed to insert preferences of student with student-id: "+sid+" with error: "+e.getMessage(),e);
        }
    }

    @Override
    public Map<String, List<?>> fetchPreferenceDetailsBySid(String sid) {
        // Keys for the map containing preference form details separately.
        final String STUDENT_REQUIREMENTS_KEY="studentRequirements";
        final String COURSE_PREFERENCES_KEY="coursePreferences";
        final String SLOT_PREFERENCES_KEY="slotPreferences";

        ConcurrentHashMap<String, List<?>> preferenceDetailsMap=new ConcurrentHashMap<>();

        CompletableFuture<Void> fetchingPreviouslySubmittedRequirements=CompletableFuture.supplyAsync(() -> studentReqService.fetchStudentRequirements(sid))
                .thenAccept((studentReqs) -> {
                    if(studentReqs!=null) preferenceDetailsMap.put(STUDENT_REQUIREMENTS_KEY,studentReqs);
                });
        CompletableFuture<Void> fetchingPreviouslySubmittedCoursePref=CompletableFuture.supplyAsync(() -> coursePrefService.fetchStudentCoursePref(sid))
                .thenAccept((coursePrefs) -> {
                    if(coursePrefs!=null) preferenceDetailsMap.put(COURSE_PREFERENCES_KEY,coursePrefs);
                });
        CompletableFuture<Void> fetchingPreviouslySubmittedSlotPref=CompletableFuture.supplyAsync(() -> slotPrefService.fetchStudentSlotPref(sid))
                .thenAccept((slotPrefs) -> {
                    if(slotPrefs!=null) preferenceDetailsMap.put(SLOT_PREFERENCES_KEY,slotPrefs);
                });

        try {
            CompletableFuture.allOf(fetchingPreviouslySubmittedRequirements, fetchingPreviouslySubmittedCoursePref, fetchingPreviouslySubmittedSlotPref).join();
        } catch (CompletionException ce) {
            log.error("Async task to fetch preference-details failed with error: {}", ce.getCause().getMessage(), ce.getCause());
            return null;
        }

        return preferenceDetailsMap;
    }
}
