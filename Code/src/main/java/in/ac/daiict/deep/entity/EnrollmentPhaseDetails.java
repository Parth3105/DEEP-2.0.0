package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.entity.compositekeys.EnrollmentPhaseDetailsPK;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "enrollment_phase_details")
@IdClass(EnrollmentPhaseDetailsPK.class)
public class EnrollmentPhaseDetails {
    @Id
    @Column(length = 10, nullable = false)
    private String program;

    @Id
    @Column(nullable = false)
    private int semester;

    @Column(name = "enrollment_phase",length = 100,nullable = false)
    private String enrollmentPhase;

    @Column(name = "collection_window_state",length = 100,nullable = false)
    private String collectionWindowState;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "result_state",length = 100,nullable = false)
    private String resultState;
}
