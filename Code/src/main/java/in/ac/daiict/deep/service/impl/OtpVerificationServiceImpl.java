package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.deadlines.Deadline;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.entity.OtpVerification;
import in.ac.daiict.deep.repository.OtpVerificationRepo;
import in.ac.daiict.deep.service.EmailService;
import in.ac.daiict.deep.service.OtpVerificationService;
import in.ac.daiict.deep.dto.ResponseDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class OtpVerificationServiceImpl implements OtpVerificationService {
    private OtpVerificationRepo otpVerificationRepo;
    private EmailService emailService;

    @Override
    public void generateOtpAndSendMail(String username, String email) {
        String otp=generateOtp();
        while(otpVerificationRepo.existsByOtp(otp)) otp=generateOtp();
        String finalOtp = otp;
        CompletableFuture.runAsync(() -> otpVerificationRepo.save(new OtpVerification(username, finalOtp, LocalDateTime.now().plusMinutes(Deadline.OTP_EXPIRATION_MINUTES))));
        CompletableFuture.runAsync(() -> emailService.sendOtp(username,email,finalOtp));
    }

    private String generateOtp(){
        return String.valueOf(new Random().nextInt(100000,999999));
    }

    @Override
    public ResponseDto verifyOtp(String username, String otp) {
        OtpVerification otpVerification=otpVerificationRepo.findById(username).orElse(null);
        if(otpVerification==null || otpVerification.getExpiryTime().isBefore(LocalDateTime.now())) return new ResponseDto(ResponseStatus.GONE, ResponseMessage.OTP_EXPIRED);
        if(otpVerification.getOtp().equals(otp)) return new ResponseDto(ResponseStatus.OK, ResponseMessage.SUCCESS);
        return new ResponseDto(ResponseStatus.UNAUTHORIZED,ResponseMessage.OTP_INVALID);
    }

    @Transactional
    @Scheduled(fixedRate = Deadline.DATABASE_REFRESH_RATE_FOR_OTP) // Every 1 Hour
    public void deleteExpiredOtp() {
        otpVerificationRepo.deleteByExpiryTimeBefore(LocalDateTime.now());
    }
}
