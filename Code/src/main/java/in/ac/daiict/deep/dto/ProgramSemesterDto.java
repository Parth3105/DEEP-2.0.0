package in.ac.daiict.deep.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ProgramSemesterDto {
    private String program;
    private int semester;
}
