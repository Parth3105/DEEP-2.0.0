package in.ac.daiict.deep.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EnrollmentPhaseDetailsDto {
    private String program;
    private int semester;
    private String enrollmentPhase;
    private String collectionWindowState;
    private LocalDate endDate;
    private String resultState;
    private String allocationState;
    long totalStudents;
    long preferenceSubmissionCnt;

    public EnrollmentPhaseDetailsDto(String program, int semester, String collectionWindowState) {
        this.program = program;
        this.semester = semester;
        this.collectionWindowState = collectionWindowState;
    }

    public EnrollmentPhaseDetailsDto(String program, int semester) {
        this.program = program;
        this.semester = semester;
    }
}