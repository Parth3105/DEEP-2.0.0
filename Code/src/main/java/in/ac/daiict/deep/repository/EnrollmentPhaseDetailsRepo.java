package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.dto.EnrollmentPhaseDetailsDto;
import in.ac.daiict.deep.dto.ProgramSemesterDto;
import in.ac.daiict.deep.entity.EnrollmentPhaseDetails;
import in.ac.daiict.deep.entity.compositekeys.EnrollmentPhaseDetailsPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface EnrollmentPhaseDetailsRepo extends JpaRepository<EnrollmentPhaseDetails, EnrollmentPhaseDetailsPK> {
    @Transactional
    @Modifying
    @Query("UPDATE EnrollmentPhaseDetails epd SET epd.endDate=:close_date, epd.collectionWindowState=:state WHERE epd.program=:program AND epd.semester=:semester")
    int updateCollectionWindowStateAndCloseDate(@Param("program") String program, @Param("semester") int semester, @Param("close_date") LocalDate closeDate, @Param("state") String state);

    @Transactional
    @Modifying
    @Query("UPDATE EnrollmentPhaseDetails epd SET epd.endDate=:close_date WHERE epd.program=:program AND epd.semester=:semester")
    int updateCloseDate(@Param("program") String program, @Param("semester") int semester, @Param("close_date") LocalDate closeDate);

    @Transactional
    @Modifying
    @Query("UPDATE EnrollmentPhaseDetails epd SET epd.collectionWindowState=:state WHERE epd.program=:program AND epd.semester=:semester")
    int updateCollectionWindowState(@Param("program") String program, @Param("semester") int semester, @Param("state") String state);

    @Transactional
    @Modifying
    @Query("UPDATE EnrollmentPhaseDetails epd SET epd.resultState=:state WHERE epd.program=:program AND epd.semester=:semester")
    int updateResultState(@Param("program") String program, @Param("semester") int semester, @Param("state") String state);

    @Query("SELECT new in.ac.daiict.deep.dto.EnrollmentPhaseDetailsDto(epd.program,epd.semester,epd.collectionWindowState) FROM EnrollmentPhaseDetails epd")
    List<EnrollmentPhaseDetailsDto> findAllCollectionWindowState();

    @Query("SELECT new in.ac.daiict.deep.dto.ProgramSemesterDto(epd.program,epd.semester) FROM EnrollmentPhaseDetails epd")
    List<ProgramSemesterDto> findAllProgramAndSemester();

    @Query("SELECT epd.collectionWindowState FROM EnrollmentPhaseDetails epd WHERE epd.program=:program AND epd.semester=:semester")
    String findCollectionWindowStateByProgramAndSemester(@Param("program") String program, @Param("semester") int semester);

    @Query("SELECT epd.resultState FROM EnrollmentPhaseDetails epd WHERE epd.program=:program AND epd.semester=:semester")
    String findResultStateByProgramAndSemester(@Param("program") String program, @Param("semester") int semester);
}
