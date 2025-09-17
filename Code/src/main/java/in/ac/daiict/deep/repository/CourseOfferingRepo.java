package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.entity.CourseOffering;
import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.CourseOfferingPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseOfferingRepo extends JpaRepository<CourseOffering,CourseOfferingPK> {
    @Override
    @Modifying
    @Query(value = "DELETE FROM "+DBConstants.WORKING_SCHEMA+"."+ DBConstants.COURSE_OFFERING_TABLE,nativeQuery = true)
    void deleteAll();

    List<CourseOffering> findByProgramAndSemester(String program,int semester);

    @Query("SELECT COUNT(offer) > 0 FROM CourseOffering offer")
    boolean existsAnyOffer();
}
