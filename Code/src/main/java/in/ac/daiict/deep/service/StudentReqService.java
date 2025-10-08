package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.StudentReqDto;
import in.ac.daiict.deep.entity.StudentReq;

import java.util.List;

public interface StudentReqService {
    List<StudentReq> fetchStudentReqByProgramAndSemester(String program, int semester);
    void insertAll(List<StudentReq> studentReqList);
    void deleteBySid(String sid);
    List<StudentReqDto> fetchStudentRequirements(String sid);
}
