package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.CourseOfferingPK;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = DBConstants.COURSE_OFFERING_TABLE)
@IdClass(CourseOfferingPK.class)
public class CourseOffering {
    @Id
    @NotBlank(message = "Seat Matrix: Program field should not be blank.")
    @Column(length = 10)
    private String program;

    @Id
    @NotBlank(message = "Seat Matrix: Course-ID should not be blank.")
    @Column(length = 10)
    private String cid;

    @Id
    @Positive(message = "Seat Matrix: Semester > 0") @Max(value = 8, message = "Seat Matrix: Semester <= 8")
    private int semester;

    @NotBlank(message = "Seat Matrix: Category field should not be blank.")
    @Column(length = 10, nullable = false)
    private String category;

    @PositiveOrZero(message = "Seat Matrix: seats >= 0")
    @Column(nullable = false)
    private int seats;
}
