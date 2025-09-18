package in.ac.daiict.deep.util.dataloader.csvHeaders;

public enum SeatMatrixHeader {
    COURSE_ID("Course ID"),
    PROGRAM("Program"),
    SEMESTER("Semester"),
    CATEGORY("Category"),
    SEATS("Seats");

    private final String header;

    SeatMatrixHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
