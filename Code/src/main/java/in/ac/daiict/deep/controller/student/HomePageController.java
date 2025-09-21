package in.ac.daiict.deep.controller.student;

import in.ac.daiict.deep.constant.endpoints.StudentEndpoint;
import in.ac.daiict.deep.constant.template.StudentTemplate;
import in.ac.daiict.deep.dto.EnrollmentPhaseDetailsDto;
import in.ac.daiict.deep.entity.Student;
import in.ac.daiict.deep.security.auth.CustomUserDetails;
import in.ac.daiict.deep.service.EnrollmentPhaseDetailsService;
import in.ac.daiict.deep.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class HomePageController {

    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    private StudentService studentService;

    @GetMapping(StudentEndpoint.HOME_PAGE)
    public String renderStudentHomePage(Model model){
        CustomUserDetails customUserDetails= (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Student student=studentService.fetchStudentData(customUserDetails.getUsername());
        if(student==null){
            return StudentTemplate.HOME_PAGE;
        }

        EnrollmentPhaseDetailsDto enrollmentPhaseDetailsDto=enrollmentPhaseDetailsService.fetchEnrollmentPhaseDetailsByProgramAndSemester(student.getProgram(),student.getSemester());
        model.addAttribute("homePageDetails",enrollmentPhaseDetailsDto);
        return StudentTemplate.HOME_PAGE;
    }

    @GetMapping(StudentEndpoint.FAQ)
    public String renderFaqPage(){
        return StudentTemplate.FAQ_PAGE;
    }

    @GetMapping(StudentEndpoint.GUIDELINES)
    public String renderGuidelinesPage(){
        return StudentTemplate.GUIDELINES_PAGE;
    }
}
