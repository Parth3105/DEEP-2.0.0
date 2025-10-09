/*
package in.ac.daiict.deep.unit.repository;

import in.ac.daiict.deep.entity.CoursePref;
import in.ac.daiict.deep.repository.CoursePrefRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CoursePrefRepoTest {
    @Autowired
    private CoursePrefRepo coursePrefRepo;

    @Test
    void test_findByProgramAndSemester(){
        List<CoursePref> coursePrefList=coursePrefRepo.findByProgramAndSemester("ICT",6);
        System.out.println("Hii: testing query: size is "+coursePrefList.size());
        coursePrefList.forEach(System.out::println);
    }
}
*/
