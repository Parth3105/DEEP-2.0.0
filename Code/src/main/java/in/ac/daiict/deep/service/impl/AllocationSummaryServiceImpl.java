package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.dto.AllocationSummaryDto;
import in.ac.daiict.deep.entity.AllocationSummary;
import in.ac.daiict.deep.entity.compositekeys.AllocationSummaryPK;
import in.ac.daiict.deep.repository.AllocationSummaryRepo;
import in.ac.daiict.deep.service.AllocationSummaryService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AllocationSummaryServiceImpl implements AllocationSummaryService {

    private AllocationSummaryRepo allocationSummaryRepo;
    private ModelMapper modelMapper;

    @Override
    public void insertAllocationSummary(AllocationSummary allocationSummary) {
        allocationSummaryRepo.save(allocationSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationSummaryDto> fetchAll() {
        List<AllocationSummary> allocationStatusList= allocationSummaryRepo.findAll();
        if(allocationStatusList.isEmpty()) return null;
        return modelMapper.map(allocationStatusList,new TypeToken<List<AllocationSummaryDto>>(){}.getType());
    }

    @Override
    public boolean checkIfExists(String program, int semester) {
        return allocationSummaryRepo.existsById(new AllocationSummaryPK(program,semester));
    }
}
