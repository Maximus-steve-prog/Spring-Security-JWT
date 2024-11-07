package com.steven.work_meet.Services;

import com.steven.work_meet.DataTransfertObject.RegisterUserDto;
import com.steven.work_meet.Entities.UserEntity;
import com.steven.work_meet.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<UserEntity> AllUsers() {
        List<UserEntity> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public Optional<UserEntity> GetUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserEntity UpdateUser(UserEntity userDto) {
        return userRepository.save(userDto);
    }
}
