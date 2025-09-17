package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.StudentDto;
import in.ac.daiict.deep.dto.UploadStatusDto;
import in.ac.daiict.deep.entity.Student;
import in.ac.daiict.deep.dto.ResponseDto;

import java.util.List;

public interface StudentService {
    ResponseDto insertAll(byte[] studentData);
    List<StudentDto> fetchAllStudentDtos();
    List<Student> fetchAllStudents();
    List<Student> fetchStudentsByProgramAndSem(String program, int semester);
    void deleteAll();
    long countBySemester(int semester);
    long countAllStudents();
    Student fetchStudentData(String sid);
    StudentDto fetchStudentDto(String sid);
    boolean fetchEnrollmentStatusForStudent(String sid);
    long countEnrolledStudents();
    void updateEnrollmentStatus(String sid);
    List<UploadStatusDto> fetchStudentDataUploadStatus();
}
