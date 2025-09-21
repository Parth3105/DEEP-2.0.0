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
    ResponseDto updateOnExtendingCollectionPeriod(String program, int semester, LocalDate closeDate);
    ResponseDto updateOnEndingPreferenceCollection(String program, int semester);
    void autoCloseRegistration();
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
