package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.dto.AllocationSummaryDto;
import in.ac.daiict.deep.entity.AllocationSummary;
import in.ac.daiict.deep.entity.compositekeys.AllocationSummaryPK;
import in.ac.daiict.deep.repository.AllocationSummaryRepo;
import in.ac.daiict.deep.service.AllocationSummaryService;
import in.ac.daiict.deep.service.StudentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@AllArgsConstructor
public class AllocationSummaryServiceImpl implements AllocationSummaryService {

    private AllocationSummaryRepo allocationSummaryRepo;
    private StudentService studentService;
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
        List<AllocationSummaryDto> allocationSummaryDtoList=new ArrayList<>();
        for(AllocationSummary allocationSummary: allocationStatusList) allocationSummaryDtoList.add(new AllocationSummaryDto(allocationSummary.getProgram(),allocationSummary.getSemester(),allocationSummary.getAllocatedCount(),allocationSummary.getUnallocatedCount()));
        return allocationSummaryDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationSummaryDto> fetchAllWithCalculatedTotalStudents() {
        List<AllocationSummary> allocationSummaryList= allocationSummaryRepo.findAll(Sort.by("program","semester").ascending());
        if(allocationSummaryList.isEmpty()) return null;

        List<CompletableFuture<AllocationSummaryDto>> futureAllocationSummaryPreparation=new ArrayList<>();
        for(AllocationSummary allocationSummary: allocationSummaryList){
            CompletableFuture<AllocationSummaryDto> futureAllocationSummaryDto=CompletableFuture.supplyAsync(() -> prepareAllocationSummary(allocationSummary));
            futureAllocationSummaryPreparation.add(futureAllocationSummaryDto);
        }

        CompletableFuture.allOf(futureAllocationSummaryPreparation.toArray(new CompletableFuture[0])).join();

        List<AllocationSummaryDto> allocationSummaryDtoList=new ArrayList<>();
        try {
            for (CompletableFuture<AllocationSummaryDto> futureAllocationSummaryDto : futureAllocationSummaryPreparation) allocationSummaryDtoList.add(futureAllocationSummaryDto.get());
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting enrollment phase details.", e);
            } else
                log.error("Async task to fetch/prepare enrollment phase details failed with error: {}", e.getCause().getMessage(), e.getCause());

            return null;
        }
        return allocationSummaryDtoList;
    }

    private AllocationSummaryDto prepareAllocationSummary(AllocationSummary allocationSummary){
        return new AllocationSummaryDto(allocationSummary.getProgram(),allocationSummary.getSemester(),allocationSummary.getAllocatedCount(),allocationSummary.getUnallocatedCount(),studentService.countStudentsByProgramAndSemester(allocationSummary.getProgram(), allocationSummary.getSemester()));
    }

    @Override
    public boolean checkIfExists(String program, int semester) {
        return allocationSummaryRepo.existsById(new AllocationSummaryPK(program,semester));
    }
}
