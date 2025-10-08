package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.dto.CoursePrefDto;
import in.ac.daiict.deep.entity.CoursePref;
import in.ac.daiict.deep.entity.SlotPref;
import in.ac.daiict.deep.entity.compositekeys.CoursePrefPK;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoursePrefRepo extends JpaRepository<CoursePref, CoursePrefPK> {

    @Query("SELECT new in.ac.daiict.deep.dto.CoursePrefDto(cpref.slot,cpref.pref,course.name,cpref.cid) FROM Course course JOIN CoursePref cpref ON course.cid=cpref.cid WHERE cpref.sid=:sid ORDER BY cpref.slot, cpref.pref ASC")
    List<CoursePrefDto> findStudentCoursePref(@Param("sid") String sid);

    @Modifying
    @Query("SELECT new CoursePref(coursePref.sid,coursePref.slot,coursePref.pref,coursePref.cid) FROM CoursePref coursePref JOIN Student student ON coursePref.sid=student.sid WHERE student.program=:program AND student.semester=:semester ORDER BY coursePref.sid, coursePref.slot, coursePref.pref ASC")
    List<CoursePref> findByProgramAndSemesterOrderBySidAscSlotAscPrefAsc(@Param("program") String program, @Param("semester") int semester);

    @Modifying
    @Query("SELECT cp FROM CoursePref cp JOIN Student s on cp.sid=s.sid WHERE s.program=:program AND s.semester=:semester ORDER BY cp.sid, cp.pref ASC")
    List<CoursePref> findByProgramAndSemester(@Param("program") String program, @Param("semester") int semester);

    void deleteBySid(String sid);
}
