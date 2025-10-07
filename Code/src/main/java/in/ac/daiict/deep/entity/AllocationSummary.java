package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.entity.compositekeys.AllocationSummaryPK;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "allocation_summary")
@IdClass(AllocationSummaryPK.class)
public class AllocationSummary {
    @Id
    @Column
    private String program;

    @Id
    @Column
    private int semester;

    @Column(nullable = false, name = "allocated_count")
    private int allocatedCount;

    @Column(nullable = false, name = "unallocated_count")
    private int unallocatedCount;

    @Column(nullable = false, name = "last_execution_time")
    private LocalDateTime lastExecutionTime;
}
