/*
package in.ac.daiict.deep.util.dataloader;

import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.service.AllocationResultService;
import in.ac.daiict.deep.service.CourseService;
import in.ac.daiict.deep.util.dataloader.impl.ExcelDataLoader;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
public class DataLoaderTest {

    @Mock private CourseService courseService;
    @Mock private AllocationResultService allocationResultService;
    @Mock private Validator validator;

    @InjectMocks private ExcelDataLoader dataLoader;

    @Test
    void testStudentValidation_Violated() throws IOException {
        byte[] idNameValidation_sample= Files.readAllBytes(Path.of("src/test/resources/files/Validate_Students_ID&Name.xlsx"));
        ResponseDto responseDto=dataLoader.getStudentData(new ByteArrayInputStream(idNameValidation_sample),anyList());
        assertEquals(ResponseStatus.BAD_REQUEST,responseDto.getStatus());
    }
}
*/
