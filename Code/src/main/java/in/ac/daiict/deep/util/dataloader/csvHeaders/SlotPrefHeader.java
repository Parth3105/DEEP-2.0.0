package in.ac.daiict.deep.util.dataloader.csvHeaders;

public enum SlotPrefHeader {
    STUDENT_ID("Student ID"),
    SLOT_NO("Slot No"),
    PREFERENCE_INDEX("Preference Index");

    private final String header;

    SlotPrefHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
