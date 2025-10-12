package in.ac.daiict.deep.entity.compositekeys;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class InstituteReqPK {
    private String program;
    private int semester;
    private String category;

    /*
     * Work of lombok @EqualsAndHashCode (below is a sample)
     */
    /*@Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(!(obj instanceof InstituteReqsPK instituteReqsPK)) return false;
        return Objects.equals(program, instituteReqsPK.program) && Objects.equals(category, instituteReqsPK.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(program,category);
    }*/
}
