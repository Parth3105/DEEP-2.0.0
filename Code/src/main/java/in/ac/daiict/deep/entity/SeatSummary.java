package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.SeatSummaryPK;
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
@Table(name = DBConstants.SEAT_SUMMARY_TABLE)
@IdClass(SeatSummaryPK.class)
public class SeatSummary {
    @Id
    @Column(length = 20)
    private String cid;
    @Id
    @Column(length = 100)
    private String program;
    @Id
    private int semester;
    @Column(name = "available_seats")
    private int availableSeats;
}
