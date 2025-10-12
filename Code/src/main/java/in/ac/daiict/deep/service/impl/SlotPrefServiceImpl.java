package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.dto.SlotPrefDto;
import in.ac.daiict.deep.entity.SlotPref;
import in.ac.daiict.deep.repository.SlotPrefRepo;
import in.ac.daiict.deep.service.SlotPrefService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class SlotPrefServiceImpl implements SlotPrefService {

    private SlotPrefRepo slotPrefRepo;
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SlotPref> fetchSlotByProgramAndSemesterSortedByPref(String program, int semester) {
        return slotPrefRepo.findByProgramAndSemester(program,semester);
    }

    @Override
    public List<SlotPrefDto> fetchStudentSlotPref(String sid) {
        List<SlotPref> slotPrefList=slotPrefRepo.findBySidOrderByPrefAsc(sid);
        if(slotPrefList==null || slotPrefList.isEmpty()) return null;
        return modelMapper.map(slotPrefList,new TypeToken<List<SlotPrefDto>>(){}.getType());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotPref> fetchSlotByProgramAndSemesterSortedBySidAndPref(String program, int semester) {
        return slotPrefRepo.findByProgramAndSemesterOrderBySidAscPrefAsc(program,semester);
    }

    @Override
    public void insertAll(List<SlotPref> slotPrefList) {
        slotPrefRepo.saveAll(slotPrefList);
    }

    @Override
    public void deleteBySid(String sid) {
        slotPrefRepo.deleteBySid(sid);
    }
}
