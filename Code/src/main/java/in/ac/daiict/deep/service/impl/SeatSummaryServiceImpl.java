package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.entity.SeatSummary;
import in.ac.daiict.deep.repository.SeatSummaryRepo;
import in.ac.daiict.deep.service.SeatSummaryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class SeatSummaryServiceImpl implements SeatSummaryService {
    private SeatSummaryRepo seatSummaryRepo;

    @Override
    @Transactional
    public void insertAll(List<SeatSummary> seatSummaryList) {
        seatSummaryRepo.saveAll(seatSummaryList);
    }

    @Override
    public void deleteAll() {
        seatSummaryRepo.deleteAll();
    }
}
