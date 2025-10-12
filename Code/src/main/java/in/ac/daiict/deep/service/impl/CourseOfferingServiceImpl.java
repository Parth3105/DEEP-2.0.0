package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.CourseOfferingDto;
import in.ac.daiict.deep.entity.CourseOffering;
import in.ac.daiict.deep.repository.CourseOfferingRepo;
import in.ac.daiict.deep.service.CourseOfferingService;
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
public class CourseOfferingServiceImpl implements CourseOfferingService {
    private CourseOfferingRepo courseOfferingRepo;
    private ModelMapper modelMapper;
    private DataLoader dataLoader;

    @Override
    @Transactional
    public ResponseDto insertAll(byte[] courseOfferData) {
        List<CourseOffering> courseOffers=new ArrayList<>();
        ResponseDto status=dataLoader.getSeatMatrix(new ByteArrayInputStream(courseOfferData),courseOffers);
        if(status.getStatus()!= ResponseStatus.OK) return status;
        courseOfferingRepo.saveAll(courseOffers);
        return new ResponseDto(ResponseStatus.OK,"Seat Matrix: Data Inserted Successfully!");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseOffering> fetchAllCourseOfferings() {
        return courseOfferingRepo.findAll();
    }

    @Override
    public List<CourseOfferingDto> fetchAllCourseOfferingDtos() {
        List<CourseOffering> courseOffers=courseOfferingRepo.findAll();
        return modelMapper.map(courseOffers,new TypeToken<List<CourseOfferingDto>>(){}.getType());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseOffering> fetchCourseOfferingByProgramAndSemester(String program, int semester) {
        List<CourseOffering> courseOfferingList=courseOfferingRepo.findByProgramAndSemester(program,semester);
        if(courseOfferingList==null || courseOfferingList.isEmpty()) return null;
        return courseOfferingList;
    }

    @Override
    public void deleteAll() {
        courseOfferingRepo.deleteAll();
    }

    @Override
    public boolean isAnyOfferPresent() {
        return courseOfferingRepo.existsAnyOffer();
    }

    @Override
    public boolean isOfferPresent(String program, int semester) {
        return courseOfferingRepo.existsByProgramAndSemester(program, semester);
    }
}
