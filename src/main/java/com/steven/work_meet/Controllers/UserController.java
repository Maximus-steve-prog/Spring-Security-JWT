package com.steven.work_meet.Controllers;

import com.steven.work_meet.DataTransfertObject.RegisterUserDto;
import com.steven.work_meet.Entities.UserEntity;
import com.steven.work_meet.Services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/server")
@CrossOrigin(origins ="http://localhost:3000")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserEntity> authenticateUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity CurrentUser = (UserEntity) authentication.getPrincipal();
        return ResponseEntity.ok(CurrentUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        List<UserEntity> users = userService.AllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/resetPassword")
    public ResponseEntity<?> updateUser(@RequestBody RegisterUserDto input) {
        Optional<UserEntity> updateUserDetails = userService.GetUserByEmail(input.getEmail());
        if (updateUserDetails.isPresent()) {
            UserEntity user = updateUserDetails.get();
            if(input.getPassword().equals(input.getConfirm_Password())) {
                user.setPassword(passwordEncoder.encode(input.getPassword()));
                userService.UpdateUser(user);
                return  ResponseEntity.ok("User updated successfully");
            }
        }
        return  ResponseEntity.badRequest().body("User update failed");
    }
}
