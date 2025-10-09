/*
package in.ac.daiict.deep.unit.repository;

import in.ac.daiict.deep.entity.StudentReq;
import in.ac.daiict.deep.repository.StudentReqRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StudentReqRepoTest {

    @Autowired
    private StudentReqRepo studentReqRepo;

    @Test
    void test_findByProgramAndSemester(){
        List<StudentReq> studentReqList=studentReqRepo.findByProgramAndSemester("ICT",6);
        System.out.println("Hii: testing query: size is "+studentReqList.size());
        studentReqList.forEach(System.out::println);
    }
}
*/
