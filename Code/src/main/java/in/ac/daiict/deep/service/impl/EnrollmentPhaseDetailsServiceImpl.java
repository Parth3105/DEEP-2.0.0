package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.enums.*;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.EnrollmentPhaseDetailsDto;
import in.ac.daiict.deep.dto.ProgramSemesterDto;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.entity.EnrollmentPhaseDetails;
import in.ac.daiict.deep.entity.compositekeys.EnrollmentPhaseDetailsPK;
import in.ac.daiict.deep.repository.EnrollmentPhaseDetailsRepo;
import in.ac.daiict.deep.service.AllocationSummaryService;
import in.ac.daiict.deep.service.PreferenceCollectionTaskManager;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.service.StudentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@AllArgsConstructor
public class EnrollmentPhaseDetailsServiceImpl implements EnrollmentPhaseDetailsService {
    private EnrollmentPhaseDetailsRepo enrollmentPhaseDetailsRepo;
    private PreferenceCollectionTaskManager preferenceCollectionTaskManager;
    private AllocationSummaryService allocationSummaryService;
    private StudentService studentService;
    private ModelMapper modelMapper;

    @Override
    public void updateEnrollmentPhaseDetails() {
        CompletableFuture<List<ProgramSemesterDto>> futureFetchStudentDistinctProgramAndSemester=CompletableFuture.supplyAsync(() -> studentService.fetchDistinctProgramAndSemester());
        CompletableFuture<List<ProgramSemesterDto>> futureFetchEnrollmentPhaseDetailsProgramAndSemester=CompletableFuture.supplyAsync(() -> enrollmentPhaseDetailsRepo.findAllProgramAndSemester());

        Set<ProgramSemesterDto> distinctProgramSemester=new HashSet<>(futureFetchStudentDistinctProgramAndSemester.join());
        List<ProgramSemesterDto> presentProgramSemester=futureFetchEnrollmentPhaseDetailsProgramAndSemester.join();

        for(ProgramSemesterDto programSemester: presentProgramSemester) distinctProgramSemester.remove(programSemester);

        List<EnrollmentPhaseDetails> enrollmentPhaseDetailsList=new ArrayList<>();
        for(ProgramSemesterDto programSemester: distinctProgramSemester) enrollmentPhaseDetailsList.add(new EnrollmentPhaseDetails(programSemester.getProgram(), programSemester.getSemester(),EnrollmentPhaseEnum.MAIN.toString(),CollectionWindowStateEnum.YET_TO_OPEN.toString(), null,ResultStateEnum.PENDING.toString()));
        enrollmentPhaseDetailsRepo.saveAll(enrollmentPhaseDetailsList);
    }

    @Override
    public ResponseDto updateOnStartingPreferenceCollection(String program, int semester, LocalDate closeDate) {
        // Verify if the results are declared before starting to collect preferences.
        EnrollmentPhaseDetails enrollmentPhaseDetail=enrollmentPhaseDetailsRepo.findById(new EnrollmentPhaseDetailsPK(program,semester)).orElse(null);
        if(enrollmentPhaseDetail==null) return new ResponseDto(ResponseStatus.BAD_REQUEST, "No entry found with program:"+program+" and semester:"+semester);
        else if(enrollmentPhaseDetail.getResultState().equalsIgnoreCase(ResultStateEnum.DECLARED.toString())) return new ResponseDto(ResponseStatus.BAD_REQUEST, ResponseMessage.COLLECTION_START_FORBIDDEN_AFTER_RESULT);

        // Update collection window's state and end date.
        int updateStatus=enrollmentPhaseDetailsRepo.updateCollectionWindowStateAndCloseDate(program, semester, closeDate, CollectionWindowStateEnum.OPEN.toString());
        if(closeDate.isBefore(LocalDate.now())) updateOnEndingPreferenceCollection(program, semester);
        else preferenceCollectionTaskManager.scheduleCollection(program,semester,closeDate.atTime(23,59));
        return new ResponseDto(ResponseStatus.OK,ResponseMessage.SUCCESS);
    }

    @Override
    public void updateOnEndingPreferenceCollection(String program, int semester) {
        preferenceCollectionTaskManager.closeWindow(program,semester);
    }

    @Override
    public ResponseDto autoCloseRegistration(String program, int semester) {
        int updateStatus=enrollmentPhaseDetailsRepo.updateCollectionWindowState(program, semester, CollectionWindowStateEnum.CLOSED.toString());
        if(updateStatus==0) return new ResponseDto(ResponseStatus.BAD_REQUEST, "No entry found with program:"+program+" and semester:"+semester);
        return new ResponseDto(ResponseStatus.OK,ResponseMessage.SUCCESS);
    }

    @Override
    public ResponseDto updateOnDeclaringResults(String program, int semester) {
        // Verify if the allocation is completed before declaring results.
        boolean isAllocated=allocationSummaryService.checkIfExists(program,semester);
        if(!isAllocated) return new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.RESULT_DECLARATION_FORBIDDEN_BEFORE_ALLOCATION);

        // Update the result state.
        int updateStatus=enrollmentPhaseDetailsRepo.updateResultState(program,semester,ResultStateEnum.DECLARED.toString());
        if(updateStatus==0) return new ResponseDto(ResponseStatus.BAD_REQUEST, "No entry found with program:"+program+" and semester:"+semester);
        return new ResponseDto(ResponseStatus.OK,ResponseMessage.SUCCESS);
    }

    @Override
    public List<EnrollmentPhaseDetailsDto> fetchEnrollmentPhaseDetailsByResultState(String resultState) {
        List<EnrollmentPhaseDetails> enrollmentPhaseDetailsList=enrollmentPhaseDetailsRepo.findByResultState(resultState,Sort.by("program","semester").ascending());

        List<CompletableFuture<EnrollmentPhaseDetailsDto>> futureEnrollmentPhaseDetailsPreparation=new ArrayList<>();
        for(EnrollmentPhaseDetails enrollmentPhaseDetails: enrollmentPhaseDetailsList){
            CompletableFuture<EnrollmentPhaseDetailsDto> futureEnrollmentPhaseDetailsDto=CompletableFuture.supplyAsync(() -> prepareEnrollmentPhaseDetail(enrollmentPhaseDetails));
            futureEnrollmentPhaseDetailsPreparation.add(futureEnrollmentPhaseDetailsDto);
        }

        CompletableFuture.allOf(futureEnrollmentPhaseDetailsPreparation.toArray(new CompletableFuture[0])).join();

        List<EnrollmentPhaseDetailsDto> enrollmentPhaseDetailsDtoList=new ArrayList<>();
        try {
            for (CompletableFuture<EnrollmentPhaseDetailsDto> futureEnrollmentPhaseDetailsDto : futureEnrollmentPhaseDetailsPreparation) enrollmentPhaseDetailsDtoList.add(futureEnrollmentPhaseDetailsDto.get());
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting enrollment phase details.", e);
            } else
                log.error("Async task to fetch/prepare enrollment phase details failed with error: {}", e.getCause().getMessage(), e.getCause());

            return null;
        }
        return enrollmentPhaseDetailsDtoList;
    }

    @Override
    public String fetchCollectionWindowState(String program, int semester) {
        return enrollmentPhaseDetailsRepo.findCollectionWindowStateByProgramAndSemester(program, semester);
    }

    @Override
    public String fetchResultState(String program, int semester) {
        return enrollmentPhaseDetailsRepo.findResultStateByProgramAndSemester(program, semester);
    }

    @Override
    public EnrollmentPhaseDetailsDto fetchEnrollmentPhaseDetailsByProgramAndSemester(String program, int semester) {
        EnrollmentPhaseDetails enrollmentPhaseDetails=enrollmentPhaseDetailsRepo.findById(new EnrollmentPhaseDetailsPK(program,semester)).orElse(null);
        return modelMapper.map(enrollmentPhaseDetails, EnrollmentPhaseDetailsDto.class);
    }

    @Override
    public List<EnrollmentPhaseDetailsDto> fetchDashboardDetails() {
        List<EnrollmentPhaseDetails> enrollmentPhaseDetailsList=enrollmentPhaseDetailsRepo.findAll(Sort.by("program","semester").ascending());

        List<CompletableFuture<EnrollmentPhaseDetailsDto>> futureEnrollmentPhaseDetailsPreparation=new ArrayList<>();
        for(EnrollmentPhaseDetails enrollmentPhaseDetails: enrollmentPhaseDetailsList){
            CompletableFuture<EnrollmentPhaseDetailsDto> futureEnrollmentPhaseDetailsDto=CompletableFuture.supplyAsync(() -> prepareEnrollmentPhaseDetail(enrollmentPhaseDetails));
            futureEnrollmentPhaseDetailsPreparation.add(futureEnrollmentPhaseDetailsDto);
        }

        CompletableFuture.allOf(futureEnrollmentPhaseDetailsPreparation.toArray(new CompletableFuture[0])).join();

        List<EnrollmentPhaseDetailsDto> enrollmentPhaseDetailsDtoList=new ArrayList<>();
        try {
            for (CompletableFuture<EnrollmentPhaseDetailsDto> futureEnrollmentPhaseDetailsDto : futureEnrollmentPhaseDetailsPreparation) enrollmentPhaseDetailsDtoList.add(futureEnrollmentPhaseDetailsDto.get());
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting enrollment phase details.", e);
            } else
                log.error("Async task to fetch/prepare enrollment phase details failed with error: {}", e.getCause().getMessage(), e.getCause());

            return null;
        }
        return enrollmentPhaseDetailsDtoList;
    }

    private EnrollmentPhaseDetailsDto prepareEnrollmentPhaseDetail(EnrollmentPhaseDetails enrollmentPhaseDetails){
        String allocationState= AllocationStateEnum.PENDING.toString();
        if(allocationSummaryService.checkIfExists(enrollmentPhaseDetails.getProgram(), enrollmentPhaseDetails.getSemester())) allocationState=AllocationStateEnum.ALLOCATED.toString();
        long totalStudents=studentService.countStudentsByProgramAndSemester(enrollmentPhaseDetails.getProgram(),enrollmentPhaseDetails.getSemester());
        long prefSubmissionCnt=studentService.countEnrolledStudentsByProgramAndSemester(enrollmentPhaseDetails.getProgram(),enrollmentPhaseDetails.getSemester());
        return new EnrollmentPhaseDetailsDto(enrollmentPhaseDetails.getProgram(),enrollmentPhaseDetails.getSemester(),enrollmentPhaseDetails.getEnrollmentPhase(),enrollmentPhaseDetails.getCollectionWindowState(),enrollmentPhaseDetails.getEndDate(),enrollmentPhaseDetails.getResultState(),allocationState,totalStudents,prefSubmissionCnt);
    }

    @Override
    public List<EnrollmentPhaseDetails> fetchDetailsWithOpenCollectionWindow() {
        return enrollmentPhaseDetailsRepo.findByCollectionWindowState(CollectionWindowStateEnum.OPEN.toString());
    }

    @Override
    public List<ProgramSemesterDto> fetchAllProgramAndSemester() {
        return enrollmentPhaseDetailsRepo.findAllProgramAndSemester();
    }

    /*
    @Override
    public EnrollmentPhaseDetailsDto fetchAllStatus() {
        List<EnrollmentPhaseDetails> systemStatusList = enrollmentPhaseDetailsRepo.findAll();
        EnrollmentPhaseDetailsDto enrollmentPhaseDetailsDto =new EnrollmentPhaseDetailsDto();
        for(EnrollmentPhaseDetails systemStatus: systemStatusList){
            if(systemStatus.getStatusName().equalsIgnoreCase(RegistrationStatus.getStatusName())) enrollmentPhaseDetailsDto.setRegistrationStatus(systemStatus.getStatusValue());
            else if(systemStatus.getStatusName().equalsIgnoreCase(RegistrationCloseDate.getStatusName())) enrollmentPhaseDetailsDto.setRegistrationCloseDate(systemStatus.getStatusValue());
            else if(systemStatus.getStatusName().equalsIgnoreCase(ResultStatus.getStatusName())) enrollmentPhaseDetailsDto.setResultStatus(systemStatus.getStatusValue());
            else if(systemStatus.getStatusName().equalsIgnoreCase(UpdateInstanceStatus.getStatusName())) enrollmentPhaseDetailsDto.setUpdateInstanceStatus(systemStatus.getStatusValue());
        }
        return enrollmentPhaseDetailsDto;
    }

    @Override
    public String fetchRegistrationStatus() {
        EnrollmentPhaseDetails systemStatus= enrollmentPhaseDetailsRepo.findById(RegistrationStatus.getStatusName()).orElse(null);
        if(systemStatus==null) return null;
        return systemStatus.getStatusValue();
    }

    @Override
    public String fetchResultStatus() {
        EnrollmentPhaseDetails systemStatus= enrollmentPhaseDetailsRepo.findById(ResultStatus.getStatusName()).orElse(null);
        if(systemStatus==null) return null;
        return systemStatus.getStatusValue();
    }

    @Override
    public RegistrationCloseDate fetchRegistrationCloseDate() {
        EnrollmentPhaseDetails systemStatus= enrollmentPhaseDetailsRepo.findById(RegistrationCloseDate.getStatusName()).orElse(null);
        if(systemStatus==null) return null;
        return new RegistrationCloseDate(systemStatus.getStatusValue());
    }
*/
}
