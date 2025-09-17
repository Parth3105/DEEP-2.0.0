package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = DBConstants.COURSE_TABLE)
public class Course {
    @Id
    @NotBlank(message = "Course Data: Course-ID should not be blank.")
    @Column(length = 10)
    private String cid;

    @NotBlank(message = "Course Data: Course-name should not be blank.")
    @Column(length = 100, nullable = false)
    private String name;

    @Positive(message = "Course Data: Credits > 0")
    @Max(value = 50)
    @Column(nullable = false)
    private int credits;

    @NotBlank(message = "Course Data: Slot should not be blank.")
    @Column(length = 4, nullable = false)
    private String slot;
}
