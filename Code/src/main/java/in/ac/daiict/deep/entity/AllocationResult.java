package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.AllocationResultPK;
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
@Table(name = DBConstants.ALLOCATION_RESULTS_TABLE)
@IdClass(AllocationResultPK.class)
public class AllocationResult {
    @Id
    @Column(length = 12)
    private String sid;
    @Id
    @Column(length = 20)
    private String cid;
}
