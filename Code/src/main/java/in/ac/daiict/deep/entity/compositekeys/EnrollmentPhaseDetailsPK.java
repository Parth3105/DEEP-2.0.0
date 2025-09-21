package in.ac.daiict.deep.entity.compositekeys;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class EnrollmentPhaseDetailsPK {
    private String program;
    private int semester;
}
