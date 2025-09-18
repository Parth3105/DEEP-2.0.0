package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.dto.CoursePrefDto;
import in.ac.daiict.deep.entity.CoursePref;
import in.ac.daiict.deep.repository.CoursePrefRepo;
import in.ac.daiict.deep.service.CoursePrefService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CoursePrefServiceImpl implements CoursePrefService {

    private CoursePrefRepo coursePrefRepo;

    @Override
    @Transactional(readOnly = true)
    public List<CoursePref> fetchCoursePrefByProgramAndSemesterSortedByPref(String program, int semester) {
        return coursePrefRepo.findByProgramAndSemester(program, semester);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursePref> fetchCoursePrefByProgramAndSemesterSortedBySlotAndPref(String program, int semester) {
        return coursePrefRepo.findByProgramAndSemesterOrderBySidAscSlotAscPrefAsc(program,semester);
    }

    @Override
    public List<CoursePrefDto> fetchStudentCoursePref(String sid) {
        List<CoursePrefDto> coursePrefDtoList=coursePrefRepo.findStudentCoursePref(sid);
        if(coursePrefDtoList==null || coursePrefDtoList.isEmpty()) return null;
        return coursePrefDtoList;
    }

    @Override
    public void insertAll(List<CoursePref> coursePrefList) {
        coursePrefRepo.saveAll(coursePrefList);
    }
}
