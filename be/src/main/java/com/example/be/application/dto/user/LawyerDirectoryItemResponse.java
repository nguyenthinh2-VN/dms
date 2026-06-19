package com.example.be.application.dto.user;

import lombok.Data;

@Data
public class LawyerDirectoryItemResponse {
    private Long id;
    private String fullName;
    private String rankLevel;
    private String specialty;
    private Integer yearsOfExperience;
}
