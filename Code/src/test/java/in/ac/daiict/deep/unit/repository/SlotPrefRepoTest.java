/*
package in.ac.daiict.deep.unit.repository;

import in.ac.daiict.deep.entity.SlotPref;
import in.ac.daiict.deep.repository.SlotPrefRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SlotPrefRepoTest {
    @Autowired
    private SlotPrefRepo slotPrefRepo;

    @Test
    void test_findByProgramAndSemester(){
        List<SlotPref> slotPrefList=slotPrefRepo.findByProgramAndSemester("ICT",6);
        System.out.println("Hii: testing query: size is "+slotPrefList.size());
        slotPrefList.forEach(System.out::println);
    }
}
*/
