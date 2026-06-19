package com.example.be.application.dto.user;

import lombok.Data;

@Data
public class UpdateMyProfileRequest {
    private String rankLevel;
    private String specialty;
    private Integer yearsOfExperience;
}
