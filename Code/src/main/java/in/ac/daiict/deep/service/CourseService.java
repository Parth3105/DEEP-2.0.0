package in.ac.daiict.deep.service;


import in.ac.daiict.deep.dto.AvailableCourseDto;
import in.ac.daiict.deep.dto.CourseDto;
import in.ac.daiict.deep.entity.Course;
import in.ac.daiict.deep.dto.ResponseDto;

import java.util.List;

public interface CourseService {
    ResponseDto insertAll(byte[] courseData);

    List<Course> fetchAllCourses();

    List<CourseDto> fetchAllCourseDtos();

    void deleteAll();

    boolean isPresent(String cid);

    List<AvailableCourseDto> fetchAvailableCourses(String program, int semester);

    long fetchCourseCnt();
}
