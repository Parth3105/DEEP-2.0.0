package in.ac.daiict.deep.security.config;

import in.ac.daiict.deep.security.constant.Roles;
import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.endpoints.CommonEndPoint;
import in.ac.daiict.deep.constant.endpoints.StudentEndpoint;
import in.ac.daiict.deep.security.handler.CustomAuthSuccessHandler;
import in.ac.daiict.deep.security.auth.CustomUserDetailService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private CustomUserDetailService customUserDetailService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/admin/images/**", "/student/images/**", "/common/js/**",
                                        "/admin/js/**", "/student/js/**", "/css/**", "/services/**", "/utils/**",CommonEndPoint.FORGOT_PASSWORD,
                                        CommonEndPoint.RESET_PASSWORD, CommonEndPoint.VERIFY_OTP,
                                        CommonEndPoint.RESEND_OTP).permitAll()
                                .requestMatchers(AdminEndpoint.ADMIN_BASE+"/**").hasRole(Roles.ADMIN)
                                .requestMatchers(StudentEndpoint.STUDENT_BASE+"/**").hasRole(Roles.STUDENT)
                                .anyRequest().authenticated())
                .exceptionHandling((handle) ->
                        handle.accessDeniedPage("/error"))

                .formLogin((login) ->
                        login.loginPage(CommonEndPoint.LOGIN)
                                .loginProcessingUrl(CommonEndPoint.AUTHENTICATE)
                                .successHandler(customAuthSuccessHandler())
                                .permitAll())

                .logout((logout) ->
                        logout.logoutUrl(CommonEndPoint.LOGOUT)
                                .logoutSuccessUrl(CommonEndPoint.LOGIN)
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(customUserDetailService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public AuthenticationSuccessHandler customAuthSuccessHandler(){
        return new CustomAuthSuccessHandler();
    }
}
