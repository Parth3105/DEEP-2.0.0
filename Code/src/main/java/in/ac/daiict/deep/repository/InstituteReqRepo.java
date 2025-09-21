package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.entity.InstituteReq;
import in.ac.daiict.deep.entity.compositekeys.InstituteReqPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstituteReqRepo extends JpaRepository<InstituteReq, InstituteReqPK> {
    @Override
    @Modifying
    @Query(value = "DELETE FROM "+DBConstants.WORKING_SCHEMA+"."+ DBConstants.INST_REQ_TABLE,nativeQuery = true)
    void deleteAll();

    List<InstituteReq> findByProgramAndSemester(String program, int semester);
    List<InstituteReq> findBySemester(int semester);
    boolean existsByProgramAndSemester(String program, int semester);
}
