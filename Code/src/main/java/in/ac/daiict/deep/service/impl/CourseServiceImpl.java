package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.AvailableCourseDto;
import in.ac.daiict.deep.dto.CourseDto;
import in.ac.daiict.deep.entity.Course;
import in.ac.daiict.deep.repository.CourseRepo;
import in.ac.daiict.deep.service.CourseService;
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
public class CourseServiceImpl implements CourseService {
    private CourseRepo courseRepo;
    private ModelMapper modelMapper;
    private DataLoader dataLoader;

    @Override
    @Transactional
    public ResponseDto insertAll(byte[] courseData) {
//        deleteAll();
        List<Course> courses=new ArrayList<>();
        ResponseDto status=dataLoader.getCourseData(new ByteArrayInputStream(courseData),courses);
        if(status.getStatus()!= ResponseStatus.OK) return status;
        courseRepo.saveAll(courses);
        return new ResponseDto(ResponseStatus.OK,"Course Data: "+ ResponseMessage.UPLOAD_SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> fetchAllCourses() {
        return courseRepo.findAll();
    }

    @Override
    public List<CourseDto> fetchAllCourseDtos() {
        List<Course> courses=courseRepo.findAll();
        return modelMapper.map(courses,new TypeToken<List<CourseDto>>(){}.getType());
    }

    @Override
    public void deleteAll() {
        courseRepo.deleteAll();
    }

    @Override
    public boolean isPresent(String cid) {
        return courseRepo.existsById(cid);
    }

    @Override
    public List<AvailableCourseDto> fetchAvailableCourses(String program, int semester) {
        return courseRepo.fetchAvailableCourses(program,semester);
    }

    @Override
    public long fetchCourseCnt() {
        return courseRepo.count();
    }
}
