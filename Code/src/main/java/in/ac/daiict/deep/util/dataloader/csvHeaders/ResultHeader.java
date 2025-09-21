package in.ac.daiict.deep.util.dataloader.csvHeaders;

public enum ResultHeader {
    STUDENT_ID("Student ID"),
    PROGRAM("Program"),
    SEMESTER("Semester"),
    COURSE_ID("Course ID"),
    COURSE_NAME("Course Name"),
    CATEGORY("Category"),
    SLOT("Slot"),
    PRIORITY("Priority"),
    CUMULATIVE_PRIORITY("Cumulative Priority");

    private final String header;

    ResultHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
