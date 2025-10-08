package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.SlotPrefDto;
import in.ac.daiict.deep.entity.SlotPref;

import java.util.List;

public interface SlotPrefService {
    List<SlotPref> fetchSlotByProgramAndSemesterSortedByPref(String program, int semester);
    List<SlotPrefDto>  fetchStudentSlotPref(String sid);
    List<SlotPref> fetchSlotByProgramAndSemesterSortedBySidAndPref(String program, int semester);
    void insertAll(List<SlotPref> slotPrefList);
    void deleteBySid(String sid);
}
