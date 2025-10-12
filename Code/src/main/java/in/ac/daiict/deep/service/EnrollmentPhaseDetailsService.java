package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.EnrollmentPhaseDetailsDto;
import in.ac.daiict.deep.dto.ProgramSemesterDto;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.entity.EnrollmentPhaseDetails;

import java.time.LocalDate;
import java.util.List;

public interface EnrollmentPhaseDetailsService {
    void updateEnrollmentPhaseDetails();
    ResponseDto updateOnStartingPreferenceCollection(String program, int semester, LocalDate closeDate);
    void updateOnEndingPreferenceCollection(String program, int semester);
    ResponseDto autoCloseRegistration(String program, int semester);
    ResponseDto updateOnDeclaringResults(String program, int semester);
    List<EnrollmentPhaseDetailsDto> fetchEnrollmentPhaseDetailsByResultState(String resultState);
    String fetchCollectionWindowState(String program, int semester);
    String fetchResultState(String program, int semester);
    EnrollmentPhaseDetailsDto fetchEnrollmentPhaseDetailsByProgramAndSemester(String program, int semester);
    List<EnrollmentPhaseDetailsDto> fetchDashboardDetails();
    List<EnrollmentPhaseDetails> fetchDetailsWithOpenCollectionWindow();
    List<ProgramSemesterDto> fetchAllProgramAndSemester();
}
