package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.entity.compositekeys.AllocationSummaryPK;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column(nullable = false)
    private int allocatedCount;
    @Column(nullable = false)
    private int unallocatedCount;
}
