package in.ac.daiict.deep.util.dataloader.csvHeaders;

public enum SeatSummaryHeader {
    COURSE_ID("Course ID"),
    COURSE_NAME("Course Name"),
    PROGRAM("Program"),
    SEMESTER("Semester"),
    CATEGORY("Category"),
    AVAILABLE_SEATS("Available Seats");

    private final String header;

    SeatSummaryHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
