package in.ac.daiict.deep.service;

import in.ac.daiict.deep.entity.AllocationReport;

import java.io.ByteArrayOutputStream;

public interface AllocationReportService {
    void insertReport(AllocationReport allocationReport);
    void deleteReport(AllocationReport allocationReport);
    ByteArrayOutputStream fetchReportsByProgramAndSemesterAsZip(String program, int semester);
}
