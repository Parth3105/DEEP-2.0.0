package in.ac.daiict.deep.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentDto {
    private String sid;
    private String name;
    private String program;
    private int semester;

    public StudentDto(String program, int semester) {
        this.program = program;
        this.semester = semester;
    }
}
