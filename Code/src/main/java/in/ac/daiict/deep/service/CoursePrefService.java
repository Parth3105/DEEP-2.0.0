package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.CoursePrefDto;
import in.ac.daiict.deep.entity.CoursePref;

import java.util.List;

public interface CoursePrefService {
    List<CoursePref> fetchCoursePrefByProgramAndSemesterSortedByPref(String program, int semester);
    List<CoursePref> fetchCoursePrefByProgramAndSemesterSortedBySlotAndPref(String program, int semester);
    List<CoursePrefDto> fetchStudentCoursePref(String sid);
    void insertAll(List<CoursePref> coursePrefList);
}
