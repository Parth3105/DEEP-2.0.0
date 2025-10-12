package in.ac.daiict.deep.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SlotPrefDto {
    private String sid;
    private int pref;
    private String slot;
}
