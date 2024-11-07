package com.steven.work_meet.DataTransfertObject;

import lombok.Data;

@Data
public class VerifyUserDto {

    private String email;
    private String verificationCode;
}
