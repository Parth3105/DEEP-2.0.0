package in.ac.daiict.deep.constant.enums;

public enum EnrollmentPhaseEnum {
    MAIN("Main"),
    ADD_DROP_I("Add/Drop I"),
    ADD_DROP_II("Add/Drop II");

    private final String header;

    EnrollmentPhaseEnum(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
