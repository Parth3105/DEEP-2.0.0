package in.ac.daiict.deep.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UploadStatusDto {
    private String program;
    private int semester;
    private long studentCount;
}
