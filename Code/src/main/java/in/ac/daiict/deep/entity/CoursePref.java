package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.CoursePrefPK;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = DBConstants.COURSE_PREFERENCE_TABLE)
@IdClass(CoursePrefPK.class)
public class CoursePref {
    @Id
    @Column(length = 12)
    private String sid;

    @Id
    @Pattern(regexp = "^\\d+$", message = "Course-Preference: Slot should not be blank and must be digit.")
    @Column(length = 4)
    private String slot;

    @Id
    private int pref;

    @NotBlank(message = "Course-Preference: Course-ID should not be blank.")
    @Column(length = 10)
    private String cid;
}
