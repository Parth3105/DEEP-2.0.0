package in.ac.daiict.deep.controller.admin;

import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.template.AdminTemplate;
import in.ac.daiict.deep.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class NavigationController {
    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;
    private StudentService studentService;
    private AllocationSummaryService allocationSummaryService;

    @GetMapping(AdminEndpoint.DASHBOARD)
    public String renderDashboardPage(){
        return AdminTemplate.DASHBOARD_PAGE;
    }

    @GetMapping(AdminEndpoint.DOWNLOAD_REPORTS)
    public String renderDownloadReportsPage(){
        return AdminTemplate.DOWNLOAD_REPORTS_PAGE;
    }

    @GetMapping(AdminEndpoint.STUDENT_PREFERENCE)
    public String renderStudentPreferencesPage(){
        return AdminTemplate.STUDENTS_PREFERENCES_PAGE;
    }

    @GetMapping(AdminEndpoint.ALLOCATION_RESULTS)
    public String renderAllocationResultsPage(){
        return AdminTemplate.ALLOCATION_RESULTS_PAGE;
    }

    @GetMapping(AdminEndpoint.FAQ)
    public String renderFaqPage(){
        return AdminTemplate.FAQ_PAGE;
    }
}
