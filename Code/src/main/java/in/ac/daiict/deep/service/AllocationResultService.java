package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.AllocationResultDto;
import in.ac.daiict.deep.entity.AllocationResult;

import java.util.List;

public interface AllocationResultService {
    void bulkInsert(List<AllocationResult> allocationResultList, String program, int semester);
    List<AllocationResultDto> fetchAllocationResult(String sid, String program);
    List<AllocationResult> fetchCourseWiseAllocation(String cid);
    void deleteAll();
    boolean allocationStatusBySem(int semester);
}
