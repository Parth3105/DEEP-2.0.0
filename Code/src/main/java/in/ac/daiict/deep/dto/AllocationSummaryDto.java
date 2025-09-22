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
    private long semester;
    private long allocatedCount;
    private long unAllocatedCount;
    private long totalStudents;

    public AllocationSummaryDto(String program, long semester, long allocatedCount, long unAllocatedCount) {
        this.program = program;
        this.semester = semester;
        this.allocatedCount = allocatedCount;
        this.unAllocatedCount = unAllocatedCount;
        totalStudents = allocatedCount+unAllocatedCount;
    }
}
