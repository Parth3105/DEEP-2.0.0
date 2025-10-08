package in.ac.daiict.deep.util.allocation;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.entity.AllocationSummary;
import in.ac.daiict.deep.service.AllocationSummaryService;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.service.StudentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Slf4j
@AllArgsConstructor
@Component
public class AllocationTaskHandler {
    private ObjectProvider<AllocationSystem> allocationSystemObjectProvider;
    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    private StudentService studentService;
    private AllocationSummaryService allocationSummaryService;

    public List<ResponseDto> initiateAllocationTasks(Map<String, List<Integer>> allocationFilter){
        List<CompletableFuture<ResponseDto>> futureMultipleAllocationTask=new ArrayList<>();
        for(Map.Entry<String,List<Integer>> allocationFilterEntry: allocationFilter.entrySet()){
            String program=allocationFilterEntry.getKey();
            for(int semester: allocationFilterEntry.getValue()) futureMultipleAllocationTask.add(CompletableFuture.supplyAsync(() -> initiateAllocation(program,semester)));
        }

        List<ResponseDto> allocationTaskStatus=new ArrayList<>();
        try{
            CompletableFuture.allOf(futureMultipleAllocationTask.toArray(new CompletableFuture[0])).join();

            for(CompletableFuture<ResponseDto> completedAllocationTask: futureMultipleAllocationTask) allocationTaskStatus.add(completedAllocationTask.get());
        } catch (CompletionException ce){
            log.error("Async task to handle allocation failed with error: {}", ce.getCause().getMessage(), ce.getCause());
            allocationTaskStatus.add(new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR));
        } catch (ExecutionException | InterruptedException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting for preference data", e);
            }
            else log.error("Async task to fetch preference data failed with error: {}", e.getCause().getMessage(), e.getCause());

            allocationTaskStatus.add(new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
        }

        return allocationTaskStatus;
    }
    public ResponseDto initiateAllocation(String program, int semester){
        enrollmentPhaseDetailsService.updateOnEndingPreferenceCollection(program,semester);
        Map<String,Long> unmetReqCnt=new HashMap<>();
        CompletableFuture<ResponseDto> futureAllocationResponse=CompletableFuture.supplyAsync(() -> allocationSystemObjectProvider.getObject().initiateAllocation(program,semester,unmetReqCnt));
        CompletableFuture<Long> futureStudentCount=CompletableFuture.supplyAsync(() -> studentService.countStudentsByProgramAndSemester(program,semester));
        try {
            ResponseDto allocationResponse = futureAllocationResponse.join();
            long totalStudents = futureStudentCount.join();
            long allocatedCount = ResponseStatus.OK == allocationResponse.getStatus() ? totalStudents - unmetReqCnt.size() : 0;
            long unallocatedCount = totalStudents - allocatedCount;
            AllocationSummary allocationSummary =new AllocationSummary(program, semester, (int) allocatedCount, (int) unallocatedCount, LocalDateTime.now());
            allocationSummaryService.insertAllocationSummary(allocationSummary);

            if(allocationResponse.getStatus()!=ResponseStatus.OK){
                String msg="Allocation Response for: "+program+" Sem-"+semester+"\nReason: "+allocationResponse.getMessage();
                allocationResponse=new ResponseDto(allocationResponse.getStatus(),msg);
            }

            return allocationResponse;
        } catch (CompletionException ce){
            log.error("Async task to fetch status/summary data failed with error: {}", ce.getCause().getMessage(), ce.getCause());
           return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
