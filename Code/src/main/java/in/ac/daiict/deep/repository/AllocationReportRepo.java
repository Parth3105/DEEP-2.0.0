package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.entity.AllocationReport;
import in.ac.daiict.deep.entity.compositekeys.AllocationReportPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationReportRepo extends JpaRepository<AllocationReport, AllocationReportPK> {
    List<AllocationReport> findByProgramAndSemester(String program, int semester);
}
