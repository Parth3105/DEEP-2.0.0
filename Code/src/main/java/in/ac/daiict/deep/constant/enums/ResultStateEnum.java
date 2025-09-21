package in.ac.daiict.deep.constant.enums;

public enum ResultStateEnum {
    PENDING("Pending"),
    DECLARED("Declared");

    private final String header;

    ResultStateEnum(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}