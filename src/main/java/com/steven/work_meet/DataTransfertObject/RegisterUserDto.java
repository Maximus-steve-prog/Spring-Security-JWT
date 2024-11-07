package com.steven.work_meet.DataTransfertObject;

import lombok.Data;

@Data
public class RegisterUserDto {

    private String username;

    private String email;

    private String password;

    private String Confirm_Password;

    public RegisterUserDto(String username, String email, String password, String Confirm_Password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.Confirm_Password = Confirm_Password;
    }
}
