package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.StudentReqPK;
import jakarta.persistence.*;
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
@Table(name = DBConstants.STUDENT_REQUIREMENTS_TABLE)
@IdClass(StudentReqPK.class)
public class StudentReq {
    @Id
    @Column(length = 12)
    private String sid;

    @Id
    @Column(length = 10)
    private String category;

    @PositiveOrZero(message = "Student Requirement: Course-count >= 0")
    @Column(name = "course_cnt", nullable = false)
    private int courseCnt;

}
