package in.ac.daiict.deep.util.dataloader.csvHeaders;

public enum CoursePrefHeader {
    STUDENT_ID("Student ID"),
    SLOT("Slot"),
    COURSE_ID("Course ID"),
    PREFERENCE_INDEX("Preference Index");

    private final String header;

    CoursePrefHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
