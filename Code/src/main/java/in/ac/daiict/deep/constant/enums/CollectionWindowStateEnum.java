package in.ac.daiict.deep.constant.enums;

public enum CollectionWindowStateEnum {
    OPEN("Open"),
    CLOSED("Closed"),
    YET_TO_OPEN("Yet to Open");

    private final String header;

    CollectionWindowStateEnum(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
