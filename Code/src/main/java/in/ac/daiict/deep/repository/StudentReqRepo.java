package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.entity.StudentReq;
import in.ac.daiict.deep.entity.compositekeys.StudentReqPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentReqRepo extends JpaRepository<StudentReq, StudentReqPK> {
    List<StudentReq> findBySid(String sid);

    @Query("SELECT COUNT(DISTINCT sr.sid) FROM StudentReq sr JOIN Student s ON sr.sid=s.sid WHERE s.semester=:semester")
    long countDistinctStudentsBySemester(@Param("semester") int semester);

    @Modifying
    @Query("SELECT sr FROM StudentReq sr JOIN Student s on sr.sid=s.sid WHERE s.program=:program AND s.semester=:semester")
    List<StudentReq> findByProgramAndSemester(@Param("program") String program, @Param("semester") int semester);

    boolean existsBySid(String sid);

    void deleteBySid(String sid);
}
