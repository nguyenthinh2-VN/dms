package com.example.be.application.dto.user;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class LawyerDetailResponse {
    private Long id;
    private String fullName;
    private String workEmail;
    private String position;
    private String phoneNumber;
    private String status;
    private String roleCode;
    private String roleName;
    private String rankLevel;
    private String specialty;
    private Integer yearsOfExperience;
    private OffsetDateTime createdAt;
}
