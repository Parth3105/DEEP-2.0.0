package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.entity.AllocationSummary;
import in.ac.daiict.deep.entity.compositekeys.AllocationSummaryPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllocationSummaryRepo extends JpaRepository<AllocationSummary, AllocationSummaryPK> {
}
