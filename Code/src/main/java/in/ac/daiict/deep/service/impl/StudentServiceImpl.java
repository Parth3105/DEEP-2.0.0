package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ProgramSemesterDto;
import in.ac.daiict.deep.dto.StudentDto;
import in.ac.daiict.deep.dto.UploadStatusDto;
import in.ac.daiict.deep.entity.Student;
import in.ac.daiict.deep.repository.StudentRepo;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.service.StudentService;
import in.ac.daiict.deep.util.dataloader.DataLoader;
import in.ac.daiict.deep.dto.ResponseDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {
    private StudentRepo studentRepo;
    private ModelMapper modelMapper;
    private DataLoader dataLoader;

    @Override
    public ResponseDto insertAll(byte[] studentData) {
        List<Student> students=new ArrayList<>();
        ResponseDto status=dataLoader.getStudentData(new ByteArrayInputStream(studentData),students);
        if(status.getStatus() != ResponseStatus.OK) return status;
        studentRepo.saveAll(students);


        return new ResponseDto(ResponseStatus.OK,"Student Data: "+ ResponseMessage.UPLOAD_SUCCESS);
    }

    @Override
    public List<StudentDto> fetchAllStudentDtos() {
        List<Student> students=studentRepo.findAll();
        return modelMapper.map(students,new TypeToken<List<StudentDto>>(){}.getType());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> fetchAllStudents() {
        return studentRepo.findAll();
    }

    @Override
    public List<Student> fetchStudentsByProgramAndSem(String program, int semester) {
        List<Student> studentList=studentRepo.findByProgramAndSemester(program,semester);
        if(studentList==null || studentList.isEmpty()) return null;
        return studentList;
    }

    @Override
    public void deleteAll() {
        studentRepo.deleteAll();
    }

    @Override
    public long countBySemester(int semester) {
        return studentRepo.countBySemester(semester);
    }

    @Override
    public long countStudentsByProgramAndSemester(String program, int semester) {
        return studentRepo.countByProgramAndSemester(program,semester);
    }

    @Override
    public Student fetchStudentData(String sid) {
        return studentRepo.findById(sid).orElse(null);
    }

    @Override
    public StudentDto fetchStudentDto(String sid) {
        Student student=studentRepo.findById(sid).orElse(null);
        if(student==null) return null;
        return modelMapper.map(student, StudentDto.class);
    }

    @Override
    public boolean fetchEnrollmentStatusForStudent(String sid) {
        Student student = studentRepo.findById(sid).orElse(null);
        if(student==null) return false;
        return student.isHasEnrolled();
    }

    @Override
    public long countEnrolledStudents() {
        return studentRepo.countByHasEnrolled(true);
    }

    @Override
    public long countEnrolledStudentsByProgramAndSemester(String program, int semester) {
        return studentRepo.countByProgramAndSemesterAndHasEnrolled(program,semester,true);
    }

    @Override
    public void updateEnrollmentStatus(String sid) {
        studentRepo.updateHasEnrolled(sid);
    }

    @Override
    public List<UploadStatusDto> fetchStudentDataUploadStatus() {
        return studentRepo.findAllCountByProgramAndSem();
    }

    @Override
    public List<ProgramSemesterDto> fetchDistinctProgramAndSemester() {
        return studentRepo.findDistinctProgramAndSemester();
    }
}
