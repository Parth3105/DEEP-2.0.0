package in.ac.daiict.deep.util.dataloader;

import in.ac.daiict.deep.entity.*;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.util.allocation.model.AllocationCourse;
import in.ac.daiict.deep.util.allocation.model.AllocationStudent;
import in.ac.daiict.deep.util.allocation.model.CourseOffer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface DataLoader {
    ResponseDto getStudentData(InputStream studentData, List<Student> students);
    ResponseDto getCourseData(InputStream courseData, List<Course> courses);
    ResponseDto getInstituteRequirements(InputStream instReqData, List<InstituteReq> instituteReqs);
    ResponseDto getSeatMatrix(InputStream offerData, List<CourseOffering> courseOfferings);
    ByteArrayOutputStream createStudentPrefSheet(List<CoursePref> coursePrefList, List<SlotPref> slotPrefList);
    ByteArrayOutputStream createResultSheet(Map<String, AllocationStudent> students, Map<String, AllocationCourse> courses, Map<String, Map<String, String>> courseCategories);
    ByteArrayOutputStream createSeatSummary(List<CourseOffer> openFor, Map<String, AllocationCourse> courses, Map<String, Map<String, Integer>> availableSeats);
    ByteArrayOutputStream createCourseWiseAllocation(Map<String,AllocationCourse> courses, Map<String,AllocationStudent> students);
}
