package in.ac.daiict.deep.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentReqDto {
    private String sid;
    private String category;
    private int courseCnt;

    public StudentReqDto(String sid, String category) {
        this.sid = sid;
        this.category=category;
    }
}
