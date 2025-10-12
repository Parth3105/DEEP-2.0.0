package in.ac.daiict.deep.service;

import in.ac.daiict.deep.entity.CoursePref;
import in.ac.daiict.deep.entity.SlotPref;
import in.ac.daiict.deep.entity.StudentReq;

import java.util.List;
import java.util.Map;

public interface PreferenceCollectionService {
    void recordPreferenceDetails(String sid, List<StudentReq> studentReqList, List<CoursePref> coursePrefList, List<SlotPref> slotPrefList);
    Map<String, List<?>> fetchPreferenceDetailsBySid(String sid);
}
