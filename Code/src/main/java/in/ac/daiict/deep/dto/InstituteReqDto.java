package in.ac.daiict.deep.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InstituteReqDto {
    private String program;
    private String category;
    private int semester;
    private int courseCnt;
}
