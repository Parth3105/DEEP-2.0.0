package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.dto.ProgramSemesterDto;
import in.ac.daiict.deep.dto.StudentDto;
import in.ac.daiict.deep.dto.UploadStatusDto;
import in.ac.daiict.deep.entity.Student;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepo extends JpaRepository<Student,String>{
    @Override
    @Modifying
    @Query(value = "DELETE FROM "+DBConstants.WORKING_SCHEMA+"."+DBConstants.STUDENT_TABLE,nativeQuery = true)
    void deleteAll();

    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.hasEnrolled = true WHERE s.sid = :sid")
    void updateHasEnrolled(@Param("sid") String sid);

    long countBySemester(int semester);
    long countByProgramAndSemester(String program, int semester);
    long countByHasEnrolled(boolean hasEnrolled);
    List<Student> findByProgramAndSemester(String program, int semester);

    @Query("SELECT new in.ac.daiict.deep.dto.UploadStatusDto(s.program,s.semester,COUNT(s)) FROM Student s GROUP BY s.program, s.semester ORDER BY s.program, s.semester ASC")
    List<UploadStatusDto> findAllCountByProgramAndSem();

    @Query("SELECT DISTINCT new in.ac.daiict.deep.dto.ProgramSemesterDto(s.program,s.semester) FROM Student s")
    List<ProgramSemesterDto> findDistinctProgramAndSemester();
}
