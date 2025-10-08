package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.dto.StudentReqDto;
import in.ac.daiict.deep.entity.StudentReq;
import in.ac.daiict.deep.repository.StudentReqRepo;
import in.ac.daiict.deep.service.StudentReqService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class StudentReqServiceImpl implements StudentReqService {
    private StudentReqRepo studentReqRepo;
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StudentReq> fetchStudentReqByProgramAndSemester(String program, int semester) {
        return studentReqRepo.findByProgramAndSemester(program, semester);
    }

    @Override
    public void insertAll(List<StudentReq> studentReqList) {
        studentReqRepo.saveAll(studentReqList);
    }

    @Override
    public void deleteBySid(String sid){
        studentReqRepo.deleteBySid(sid);
    }

    @Override
    public List<StudentReqDto> fetchStudentRequirements(String sid) {
        List<StudentReq> studentReqList=studentReqRepo.findBySid(sid);
        if(studentReqList==null || studentReqList.isEmpty()) return null;
        return modelMapper.map(studentReqList, new TypeToken<List<StudentReqDto>>(){}.getType());
    }
}
