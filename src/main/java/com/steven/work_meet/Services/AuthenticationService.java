package com.steven.work_meet.Services;

import com.steven.work_meet.DataTransfertObject.LoginUserDto;
import com.steven.work_meet.DataTransfertObject.RegisterUserDto;
import com.steven.work_meet.DataTransfertObject.VerifyUserDto;
import com.steven.work_meet.Entities.UserEntity;
import com.steven.work_meet.Repositories.UserRepository;
import com.steven.work_meet.Responses.RegisterResponse;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthenticationService  {


    private final UserRepository userRepository;

    private  RegisterResponse registerResponse;

    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, AuthenticationManager authenticationManager, EmailService emailService, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }


    public ResponseEntity<RegisterResponse> signup(RegisterUserDto input) {
        Optional<UserEntity> getUser = userRepository.findByEmail(input.getEmail());
        if (getUser.isPresent()) {
            registerResponse = new RegisterResponse(25,"This email address is already in use");
        }else{
            UserEntity user = new UserEntity(input.getUsername(), input.getEmail(),passwordEncoder.encode(input.getPassword()));
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            user.setEnabled(false);
            // Send verification email
            try{
                sendVerificationEmail(user);
                userRepository.save(user);
                registerResponse = new RegisterResponse(50,"The verification Code has been on your email");
            }catch (Exception e){
                registerResponse = new RegisterResponse(100,"Please ! Check your connection or email and try again");
            }
        }
        // Generate verification code

        return ResponseEntity.ok(registerResponse);
    }


    public UserEntity authenticate(LoginUserDto input) {
        UserEntity user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("Invalid email or password"));
        if(!user.isEnabled()) {
            throw new RuntimeException("Account not verified ! Please try again or Verify your email account");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );
        return user;
    }

    public ResponseEntity<RegisterResponse> verifyUser(String email , String code) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                registerResponse = new RegisterResponse(50, "Verification code expired");
            }

            if(user.getVerificationCode().equals(code)) {
                user.setVerificationCodeExpiresAt(null);
                user.setVerificationCode(null);
                user.setEnabled(true);
                userRepository.save(user);
            }else {
                registerResponse = new RegisterResponse(100, "Invalid verification code");
            }
        }else {
            registerResponse = new RegisterResponse(100, "Invalid email or password");
        }
        return ResponseEntity.ok(registerResponse);
    }

    public void resendVerificationCode(String email) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            if(user.isEnabled()) {
                  throw new RuntimeException("Account already verified !");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        }
    }

    public void sendVerificationEmail(UserEntity user) {
        String subject = "Verification Code";
        String VerificationCode = user.getVerificationCode();
        String emailAndValidationCode= "http://localhost:3000/verify?email="+ user.getEmail()+"&code=" + VerificationCode;
        String Message = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Email Verification</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f4f4f4;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            background-color: #ffffff;\n" +
                "            padding: 20px;\n" +
                "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                "        }\n" +
                "        h1 {\n" +
                "            color: #333333;\n" +
                "        }\n" +
                "        .verification-code {\n" +
                "            display: inline-block;\n" +
                "            padding: 10px 15px;\n" +
                "            background-color:  aliceblue;\n" +
                "            box-shadow: -1px 1px 5px rgba(0, 0, 0, 0.5);"+
                "            color: white;\n" +
                "            font-size: 24px;\n" +
                "            border-radius: 10px;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "        p {\n" +
                "            color: #555555;\n" +
                "            line-height: 1.5;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            margin-top: 30px;\n" +
                "            font-size: 12px;\n" +
                "            color: #777777;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>Email Verification</h1>\n" +
                "        <p>Dear "+user.getUsername()+",</p>\n" +
                "        <p>Thank you for registering with us. To complete your registration, please verify your email address by using the verification code below:</p>\n" +
                "        <p class=\"verification-code\">"+emailAndValidationCode+"</p>\n" +
                "        <p>If you did not create an account using this email address, please disregard this email.</p>\n" +
                "        <p>Thank you!</p>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2023 Your Company. All Rights Reserved.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        try{
            emailService.sendVerificationEmail(user.getEmail(),subject,Message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000)+100000;
        return String.valueOf(code);
    }
}
