package in.ac.daiict.deep.entity.compositekeys;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AllocationSummaryPK {
    private String program;
    private int semester;
}
