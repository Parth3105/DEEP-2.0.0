package in.ac.daiict.deep.controller.common;

import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.endpoints.CommonEndPoint;
import in.ac.daiict.deep.constant.endpoints.StudentEndpoint;
import in.ac.daiict.deep.security.constant.Roles;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectBasedOnRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:"+ CommonEndPoint.LOGIN;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equalsIgnoreCase(Roles.ROLE_ADMIN));
        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equalsIgnoreCase(Roles.ROLE_STUDENT));

        if (isAdmin) {
            return "redirect:"+ AdminEndpoint.DASHBOARD;
        } else if (isStudent) {
            return "redirect:"+ StudentEndpoint.HOME_PAGE;
        } else {
            return "redirect:"+ CommonEndPoint.LOGIN;
        }
    }
}

