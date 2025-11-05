package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.AllocationReportPK;
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
@Table(name = DBConstants.ALLOCATION_REPORT_TABLE)
@IdClass(AllocationReportPK.class)
public class AllocationReport {
    @Id
    @Column(length = 100,nullable = false)
    private String name;

    @Id
    @Column(length = 100, nullable = false)
    private String program;

    @Id
    @Column(nullable = false)
    private int semester;

    @Column(columnDefinition = "BYTEA",nullable = false)
    private byte[] file;
}
