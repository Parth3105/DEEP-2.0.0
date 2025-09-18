package in.ac.daiict.deep.util.dataloader.csvHeaders;

public enum InstituteReqHeader {
    PROGRAM("Program"),
    SEMESTER("Semester"),
    CATEGORY("Category"),
    COUNT("Count");

    private final String header;

    InstituteReqHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
