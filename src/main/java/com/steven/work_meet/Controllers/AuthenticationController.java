package com.steven.work_meet.Controllers;

import com.steven.work_meet.DataTransfertObject.LoginUserDto;
import com.steven.work_meet.DataTransfertObject.RegisterUserDto;
import com.steven.work_meet.DataTransfertObject.VerifyUserDto;
import com.steven.work_meet.Entities.UserEntity;
import com.steven.work_meet.Responses.LoginResponse;
import com.steven.work_meet.Responses.RegisterResponse;
import com.steven.work_meet.Services.JwtService;
import com.steven.work_meet.Services.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/server")
@CrossOrigin(origins ="http://localhost:3000")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private  RegisterResponse registerResponse;
    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }


    @PostMapping("/auth/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
       try{
            registerResponse = authenticationService.signup(registerUserDto).getBody();
            return ResponseEntity.ok(registerResponse);
       }catch (Exception e){
           return ResponseEntity.ok(null);
       }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
       try{
           UserEntity user = authenticationService.authenticate(loginUserDto);
           String token = jwtService.generateToken(user);
           LoginResponse loginResponse = new LoginResponse(token, jwtService.getExpirationTime());
           return ResponseEntity.ok(loginResponse);
       }catch (Exception e){
           return ResponseEntity.ok(new LoginResponse(e.getMessage(), jwtService.getExpirationTime()));
       }
    }


    @PostMapping("/auth/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto input) {
        try {
            registerResponse = authenticationService.verifyUser(input.getEmail(), input.getVerificationCode()).getBody();
            return ResponseEntity.ok(registerResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(e.getMessage());
        }
    }


    @PostMapping("/auth/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam("email") String email) {
        try{
            authenticationService.resendVerificationCode(email);
            return  ResponseEntity.ok("Account resend Successfully");
        }catch (RuntimeException e){
            return ResponseEntity.ok(e.getMessage());
        }
    }
}
