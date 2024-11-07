package com.steven.work_meet.Responses;

import lombok.Data;

@Data
public class RegisterResponse {
    private long response;
    private String message;
    public RegisterResponse(long response, String message) {
        this.response = response;
        this.message = message;
    }
}
