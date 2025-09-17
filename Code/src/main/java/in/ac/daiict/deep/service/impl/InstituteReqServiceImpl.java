package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.InstituteReqDto;
import in.ac.daiict.deep.entity.InstituteReq;
import in.ac.daiict.deep.repository.InstituteReqRepo;
import in.ac.daiict.deep.service.InstituteReqService;
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
public class InstituteReqServiceImpl implements InstituteReqService {
    private InstituteReqRepo instituteReqRepo;
    private ModelMapper modelMapper;
    private DataLoader dataLoader;

    @Override
    @Transactional
    public ResponseDto insertAll(byte[] instituteReqData) {
//        deleteAll();
        List<InstituteReq> instituteReqs=new ArrayList<>();
        ResponseDto status=dataLoader.getInstituteRequirements(new ByteArrayInputStream(instituteReqData),instituteReqs);
        if(status.getStatus()!= ResponseStatus.OK) return status;
        instituteReqRepo.saveAll(instituteReqs);
        return new ResponseDto(ResponseStatus.OK,"Institute Requirement: Data Inserted Successfully!");
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstituteReq> fetchAllInstituteReqs() {
        return instituteReqRepo.findAll();
    }

    @Override
    public List<InstituteReqDto> fetchAllInstituteReqDtos() {
        List<InstituteReq> InstituteReqs=instituteReqRepo.findAll();
        return modelMapper.map(InstituteReqs,new TypeToken<List<InstituteReqDto>>(){}.getType());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstituteReq> fetchInstituteReqByProgramAndSemester(String program, int semester) {
        List<InstituteReq> instituteReqList=instituteReqRepo.findByProgramAndSemester(program, semester);
        if(instituteReqList==null || instituteReqList.isEmpty()) return null;
        return instituteReqList;
    }

    @Override
    public void deleteAll() {
        instituteReqRepo.deleteAll();
    }

    @Override
    public List<InstituteReqDto> findInstituteReq(String program, int semester) {
        List<InstituteReq> instituteReqs=instituteReqRepo.findByProgramAndSemester(program,semester);
        if(instituteReqs==null) return null;
        return modelMapper.map(instituteReqs,new TypeToken<List<InstituteReqDto>>(){}.getType());
    }
}
