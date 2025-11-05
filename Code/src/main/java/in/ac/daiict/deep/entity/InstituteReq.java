package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.InstituteReqPK;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name= DBConstants.INST_REQ_TABLE)
@IdClass(InstituteReqPK.class)
public class InstituteReq {
    @Id
    @NotBlank(message = "Institute Requirements: Program field should not be blank.")
    @Column(length = 100)
    private String program;

    @Id
    @Positive(message = "Institute Requirements: Semester > 0") @Max(value = 8, message = "Institute Requirements: Semester <= 8")
    private int semester;

    @Id
    @NotBlank(message = "Institute Requirements: Category field should not be blank.")
    @Column(length = 100)
    private String category;

    @PositiveOrZero(message = "Institute Requirements: Course-count >= 0")
    @Column(name = "course_cnt", nullable = false)
    private int courseCnt;
}
