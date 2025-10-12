package in.ac.daiict.deep.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CoursePrefDto {
    private String slot;
    private int pref;
    private String cname;
    private String cid;
}
