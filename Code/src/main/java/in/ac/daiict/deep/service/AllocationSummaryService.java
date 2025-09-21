package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.AllocationSummaryDto;
import in.ac.daiict.deep.entity.AllocationSummary;

import java.util.List;

public interface AllocationSummaryService {
    void insertAllocationSummary(AllocationSummary allocationSummary);
    List<AllocationSummaryDto> fetchAll();
    boolean checkIfExists(String program, int semester);
}
