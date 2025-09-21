package in.ac.daiict.deep.constant.enums;

public enum AllocationStateEnum {
    PENDING("Pending"),
    ALLOCATED("Allocated");

    private final String header;

    AllocationStateEnum(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
