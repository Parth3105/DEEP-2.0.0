package in.ac.daiict.deep.util.dataloader.csvHeaders;

public enum CourseWiseAllocationHeader {
    STUDENT_ID("Student ID"),
    NAME("Student Name"),
    ;

    private final String header;

    CourseWiseAllocationHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
