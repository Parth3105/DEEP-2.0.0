package in.ac.daiict.deep.entity;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.compositekeys.SlotPrefPK;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = DBConstants.SLOT_PREFERENCE_TABLE)
@IdClass(SlotPrefPK.class)
public class SlotPref {
    @Id
    @Column(length = 12)
    private String sid;

    @Id
    private int pref;

    @Pattern(regexp = "^\\d+$", message = "Slot-Preference: Slot should not be blank and must be digit.")
    @Column(length = 4)
    private String slot;
}
