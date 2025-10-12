package in.ac.daiict.deep.controller.common;

import in.ac.daiict.deep.constant.deadlines.Deadline;
import in.ac.daiict.deep.constant.endpoints.CommonEndPoint;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.template.CommonTemplate;
import in.ac.daiict.deep.entity.User;
import in.ac.daiict.deep.service.OtpVerificationService;
import in.ac.daiict.deep.service.UserService;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.util.sessionhelper.SessionAttribute;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;

@Slf4j
@SuppressWarnings("unchecked")
@Controller
@AllArgsConstructor
public class LoginController {
    private UserService userService;
    private OtpVerificationService otpVerificationService;

    @GetMapping(CommonEndPoint.REGISTER)
    public String renderRegisterPage(HttpSession session){
        return CommonTemplate.REGISTER_PAGE;
    }

    @PostMapping(CommonEndPoint.REGISTER)
    public String registerUser(@RequestParam("username") String username, @RequestParam("userEmail") String userEmail, RedirectAttributes redirectAttributes, HttpSession session){
        if(userService.findUser(username)!=null){
            redirectAttributes.addFlashAttribute("userAlreadyExists",new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.USER_ALREADY_EXISTS));
            return "redirect:"+CommonEndPoint.LOGIN;
        }
        if(!userEmail.endsWith("@dau.ac.in")){
            redirectAttributes.addFlashAttribute("invalidEmail",new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.INVALID_EMAIL));
            return "redirect:"+CommonEndPoint.REGISTER;
        }
        otpVerificationService.generateOtpAndSendMail(username, userEmail);
        session.setAttribute("emailVerificationSession", new SessionAttribute<>(username, Duration.ofMinutes(Deadline.FORGOT_PASSWORD_SESSION_DURATION_MINUTES)));
        session.setAttribute("userEmail", new SessionAttribute<>(userEmail, Duration.ofMinutes(Deadline.FORGOT_PASSWORD_SESSION_DURATION_MINUTES)));
        return CommonTemplate.VERIFY_OTP_PAGE;
    }

    @GetMapping(CommonEndPoint.LOGIN)
    public String renderLoginPage(HttpSession session) {
        session.setAttribute("loginSession", new SessionAttribute<>(true, Duration.ofMinutes(Deadline.LOGIN_SESSION_DURATION_MINUTES)));
        return CommonTemplate.LOGIN_PAGE;
    }
    @PostMapping(CommonEndPoint.AUTHENTICATE)
    public String authenticate() {
        return "";
    }

    @GetMapping(CommonEndPoint.FORGOT_PASSWORD)
    public String renderForgotPasswordPage(HttpSession session) {
        if (session.getAttribute("loginSession") == null) {
            return "redirect:" + CommonEndPoint.LOGIN;
        }
        return CommonTemplate.FORGOT_PASSWORD_PAGE;
    }
    @PostMapping(CommonEndPoint.FORGOT_PASSWORD)
    public String loadStudentId(@RequestParam("username") String username, RedirectAttributes redirectAttributes, HttpSession session) {
        if (session.getAttribute("loginSession") == null) return "redirect:" + CommonEndPoint.LOGIN;
        else {
            SessionAttribute<Boolean> sessionAttribute = (SessionAttribute<Boolean>) session.getAttribute("loginSession");
            if (sessionAttribute.isExpired()) {
                redirectAttributes.addFlashAttribute("sessionExpired", new ResponseDto(ResponseStatus.SESSION_TIMEOUT, ResponseMessage.SESSION_EXPIRED));
                return "redirect:" + CommonEndPoint.LOGIN;
            }
        }
        User user = userService.findUser(username);
        if (user == null) {
            redirectAttributes.addFlashAttribute("submitResponse", new ResponseDto(ResponseStatus.NOT_FOUND, ResponseMessage.USERNAME_NOT_FOUND));
            return "redirect:" + CommonEndPoint.FORGOT_PASSWORD;
        }
        otpVerificationService.generateOtpAndSendMail(user.getUsername(), user.getEmail());
        session.setAttribute("emailVerificationSession", new SessionAttribute<>(username, Duration.ofMinutes(Deadline.FORGOT_PASSWORD_SESSION_DURATION_MINUTES)));
        session.setAttribute("userEmail", new SessionAttribute<>(user.getEmail(), Duration.ofMinutes(Deadline.FORGOT_PASSWORD_SESSION_DURATION_MINUTES)));
        return "redirect:"+CommonEndPoint.VERIFY_OTP;
    }

    @GetMapping(CommonEndPoint.VERIFY_OTP)
    public String renderOtpVerificationPage(HttpSession session){
        if (session.getAttribute("emailVerificationSession") == null) return "redirect:" + CommonEndPoint.LOGIN;
        return CommonTemplate.VERIFY_OTP_PAGE;
    }
    @PostMapping(CommonEndPoint.RESEND_OTP)
    public String resendOtp(RedirectAttributes redirectAttributes, HttpSession session) {
        String username, email;
        if (session.getAttribute("emailVerificationSession") == null) return "redirect:" + CommonEndPoint.LOGIN;
        else {
            SessionAttribute<String> sessionAttribute = (SessionAttribute<String>) session.getAttribute("emailVerificationSession");
            SessionAttribute<String> userEmail = (SessionAttribute<String>) session.getAttribute("userEmail");
            username = sessionAttribute.getValue();
            email = userEmail.getValue();
            if (sessionAttribute.isExpired()) {
                redirectAttributes.addFlashAttribute("sessionExpired", new ResponseDto(ResponseStatus.SESSION_TIMEOUT, ResponseMessage.SESSION_EXPIRED));
                return "redirect:" + CommonEndPoint.LOGIN;
            }
        }
        otpVerificationService.generateOtpAndSendMail(username, email);
        return "redirect:"+CommonEndPoint.VERIFY_OTP;
    }
    @PostMapping(CommonEndPoint.VERIFY_OTP)
    public String loadOtp(@RequestParam("otp") String otp, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        String username;
        if (session.getAttribute("emailVerificationSession") == null) return "redirect:" + CommonEndPoint.LOGIN;
        else {
            SessionAttribute<String> sessionAttribute = (SessionAttribute<String>) session.getAttribute("emailVerificationSession");
            username = sessionAttribute.getValue();
            if (sessionAttribute.isExpired()) {
                redirectAttributes.addFlashAttribute("sessionExpired", new ResponseDto(ResponseStatus.SESSION_TIMEOUT, ResponseMessage.SESSION_EXPIRED));
                return "redirect:" + CommonEndPoint.LOGIN;
            }
        }
        ResponseDto response = otpVerificationService.verifyOtp(username, otp);
        if (response.getStatus() != ResponseStatus.OK) {
            redirectAttributes.addFlashAttribute("otpVerificationResponse",response);
            return "redirect:"+CommonEndPoint.VERIFY_OTP;
        }
        session.setAttribute("resetSession", new SessionAttribute<>(username, Duration.ofMinutes(Deadline.RESET_SESSION_DURATION_MINUTES)));
        return CommonTemplate.RESET_PASSWORD_PAGE;
    }

    @PostMapping(CommonEndPoint.RESET_PASSWORD)
    public String loadNewPassword(@RequestParam String password, RedirectAttributes redirectAttributes, HttpSession session) {
        String username, email;
        if (session.getAttribute("resetSession") == null) return "redirect:" + CommonEndPoint.LOGIN;
        else {
            SessionAttribute<String> sessionAttribute = (SessionAttribute<String>) session.getAttribute("resetSession");
            SessionAttribute<String> userEmail = (SessionAttribute<String>) session.getAttribute("userEmail");
            username = sessionAttribute.getValue();
            email = userEmail.getValue();
            if (sessionAttribute.isExpired()) {
                redirectAttributes.addFlashAttribute("sessionExpired", new ResponseDto(ResponseStatus.SESSION_TIMEOUT, ResponseMessage.SESSION_EXPIRED));
                return "redirect:" + CommonEndPoint.LOGIN;
            }
        }
        ResponseDto resetResponse;
        try{
            userService.resetPassword(username,email,password);
            resetResponse=new ResponseDto(ResponseStatus.OK,"Registered Successfully!");
        } catch (Exception e){
            log.error("Failed to register the user with email: {} with error: {}",email,e.getMessage(),e);
            resetResponse=new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.LOGIN_REGISTER_ERROR);
        }
        redirectAttributes.addFlashAttribute("resetResponse",resetResponse);
        return "redirect:" + CommonEndPoint.LOGIN;
    }
}
