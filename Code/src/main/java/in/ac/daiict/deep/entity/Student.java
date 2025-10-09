package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = DBConstants.STUDENT_TABLE)
public class Student {
    @Id
    @Size(min = 9, max = 9, message = "Student Data: Student-ID should contain 9 digits. For ex. 20220XXXX.")
    @Column(length = 9)
    private String sid;

    @NotBlank(message = "Student Data: Student-name should not be blank.")
    @Size(min = 1, max = 100, message = "Student Data: Student name must be within 100 characters.")
    @Column(length = 100, nullable = false)
    private String name;

    @NotBlank(message = "Student Data: Program field should not be blank.")
    @Size(max = 10, message = "Student Data: Program must be within 10 characters.")
    @Column(length = 10, nullable = false)
    private String program;

    @Positive(message = "Student Data: Semester > 0") @Max(value = 8, message = "Student Data: Semester <= 8")
    @Column(nullable = false)
    private int semester;

    @Column(name = "has_enrolled")
    private boolean hasEnrolled;

    public Student(String sid, String name, String program, int semester) {
        this.sid = sid;
        this.name = name;
        this.program = program;
        this.semester = semester;
        this.hasEnrolled=false;
    }
}
