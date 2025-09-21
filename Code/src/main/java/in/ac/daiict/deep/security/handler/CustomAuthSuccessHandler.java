package in.ac.daiict.deep.security.handler;


import in.ac.daiict.deep.security.constant.Roles;
import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.endpoints.StudentEndpoint;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String role=authentication.getAuthorities().iterator().next().getAuthority();
        if(role.equalsIgnoreCase(Roles.ROLE_ADMIN)) response.sendRedirect(request.getContextPath()+AdminEndpoint.DASHBOARD);
        else response.sendRedirect(request.getContextPath()+StudentEndpoint.HOME_PAGE);
    }
}