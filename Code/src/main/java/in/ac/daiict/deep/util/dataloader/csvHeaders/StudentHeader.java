package in.ac.daiict.deep.util.dataloader.csvHeaders;

public enum StudentHeader {
    STUDENT_ID("Student ID"),
    NAME("Name"),
    SEMESTER("Semester"),
    PROGRAM("Program");

    private final String header;

    StudentHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
