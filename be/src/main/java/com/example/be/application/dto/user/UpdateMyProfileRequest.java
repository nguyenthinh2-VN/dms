package com.example.be.application.dto.user;

import lombok.Data;

@Data
public class UpdateMyProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String password;
    private String rankLevel;
    private String specialty;
    private Integer yearsOfExperience;
}
