package in.ac.daiict.deep.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AllocationSummaryDto {
    private String program;
    private int semester;
    private int allocatedCount;
    private int unAllocatedCount;
}
