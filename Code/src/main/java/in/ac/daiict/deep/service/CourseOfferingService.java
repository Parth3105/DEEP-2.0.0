package in.ac.daiict.deep.service;

import in.ac.daiict.deep.dto.CourseOfferingDto;
import in.ac.daiict.deep.entity.CourseOffering;
import in.ac.daiict.deep.dto.ResponseDto;

import java.util.List;

public interface CourseOfferingService {
    ResponseDto insertAll(byte[] courseOfferData);
    List<CourseOffering> fetchAllCourseOfferings();
    List<CourseOfferingDto> fetchAllCourseOfferingDtos();
    List<CourseOffering> fetchCourseOfferingByProgramAndSemester(String program, int semester);
    void deleteAll();
    boolean isAnyOfferPresent();
    boolean isOfferPresent(String program, int semester);
}
