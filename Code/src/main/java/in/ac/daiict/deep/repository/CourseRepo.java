package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.dto.AvailableCourseDto;
import in.ac.daiict.deep.entity.Course;
import in.ac.daiict.deep.constant.database.DBConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepo extends JpaRepository<Course,String> {
    @Override
    @Modifying
    @Query(value = "DELETE FROM "+DBConstants.WORKING_SCHEMA+"."+DBConstants.COURSE_TABLE, nativeQuery = true)
    void deleteAll();
    @Query("SELECT new in.ac.daiict.deep.dto.AvailableCourseDto(course.slot,course.cid,course.name,offer.program,offer.category,course.credits) FROM Course course JOIN CourseOffering offer ON course.cid=offer.cid WHERE program=:program AND semester=:semester ORDER BY course.slot, offer.category ASC")
    List<AvailableCourseDto> fetchAvailableCourses(@Param("program") String program, @Param("semester") int semester);

    @Query("SELECT c.cid FROM Course c")
    List<String> fetchAllCid();
}
