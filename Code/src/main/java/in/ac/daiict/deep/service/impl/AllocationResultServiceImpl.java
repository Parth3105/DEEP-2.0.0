package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.dto.AllocationResultDto;
import in.ac.daiict.deep.entity.AllocationResult;
import in.ac.daiict.deep.repository.AllocationResultRepo;
import in.ac.daiict.deep.service.AllocationResultService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AllocationResultServiceImpl implements AllocationResultService {
    private AllocationResultRepo allocationResultRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void bulkInsert(List<AllocationResult> allocationResultList, String program, int semester) {
        allocationResultRepo.deleteBasedOnProgramAndSemester(program,semester);
        int batchSize = 100;

        for (int i = 0; i < allocationResultList.size(); i++) {
            entityManager.persist(allocationResultList.get(i));
            if (i % batchSize == 0 && i > 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public List<AllocationResultDto> fetchAllocationResult(String sid, String program) {
        List<AllocationResultDto> allocationResultDtoList=allocationResultRepo.fetchAllocationResultBySid(sid, program);
        if(allocationResultDtoList==null || allocationResultDtoList.isEmpty()) return null;
        return allocationResultDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResult> fetchCourseWiseAllocation(String cid) {
        return allocationResultRepo.findByCidOrderBySid(cid);
    }

    @Override
    public void deleteAll() {
        allocationResultRepo.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean allocationStatusBySem(int semester) {
        return allocationResultRepo.allocationStatusBySem(semester);
    }
}
