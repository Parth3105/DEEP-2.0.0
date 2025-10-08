package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.entity.InstanceName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstanceNameRepo extends JpaRepository<InstanceName,String> {
    Optional<InstanceName> findTopByOrderByCreatedAtDesc();
    List<InstanceName> findTop30ByOrderByCreatedAtDesc();
}
