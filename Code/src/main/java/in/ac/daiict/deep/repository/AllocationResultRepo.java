package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.dto.AllocationResultDto;
import in.ac.daiict.deep.entity.AllocationResult;
import in.ac.daiict.deep.entity.compositekeys.AllocationResultPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AllocationResultRepo extends JpaRepository<AllocationResult, AllocationResultPK> {

    List<AllocationResult> findByCidOrderBySid(String cid);

    @Query("SELECT new in.ac.daiict.deep.dto.AllocationResultDto(course.cid,course.name,offer.category,course.credits) FROM AllocationResult result JOIN Course course ON result.cid=course.cid JOIN CourseOffering offer ON course.cid=offer.cid WHERE result.sid=:sid and offer.program=:program")
    List<AllocationResultDto> fetchAllocationResultBySid(@Param("sid") String sid, @Param("program") String program);

    @Override
    @Modifying
    @Query(value = "DELETE FROM "+DBConstants.WORKING_SCHEMA+"."+ DBConstants.ALLOCATION_RESULTS_TABLE,nativeQuery = true)
    void deleteAll();

    @Query(value = "SELECT EXISTS(SELECT 1 FROM deep.allocation_results NATURAL JOIN deep.students WHERE students.semester=:semester)",nativeQuery = true)
    boolean allocationStatusBySem(@Param("semester") int semester);

    @Transactional
    @Modifying
    @Query(value = """
            DELETE FROM deep.allocation_results
            WHERE sid IN (
                SELECT deep.students.sid
                FROM deep.students
                JOIN deep.allocation_results ar ON deep.students.sid = ar.sid
                WHERE deep.students.program = :program AND deep.students.semester = :semester
            );
            """,nativeQuery = true)
    void deleteBasedOnProgramAndSemester(@Param("program") String program, @Param("semester") int semester);
}
