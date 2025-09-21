package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.EnrollmentPhaseDetailsDto;
import in.ac.daiict.deep.dto.ProgramSemesterDto;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.entity.EnrollmentPhaseDetails;
import in.ac.daiict.deep.util.status.RegistrationCloseDate;

import java.time.LocalDate;
import java.util.List;

public interface EnrollmentPhaseDetailsService {
    void updateEnrollmentPhaseDetails();
    ResponseDto updateOnStartingPreferenceCollection(String program, int semester, LocalDate closeDate);
    void updateOnEndingPreferenceCollection(String program, int semester);
    ResponseDto autoCloseRegistration(String program, int semester);
    ResponseDto updateOnDeclaringResults(String program, int semester);
    List<EnrollmentPhaseDetailsDto> fetchAllEnrollmentPhaseDetails();
    String fetchCollectionWindowState(String program, int semester);
    String fetchResultState(String program, int semester);
    EnrollmentPhaseDetailsDto fetchEnrollmentPhaseDetailsByProgramAndSemester(String program, int semester);
    List<EnrollmentPhaseDetailsDto> fetchDashboardDetails();
/*
    EnrollmentPhaseDetailsDto fetchAllStatus();
    RegistrationCloseDate fetchRegistrationCloseDate();
*/
}
