package com.example.be.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String workEmail;
    private String position;
    private String phoneNumber;
    private String status;
    private String roleCode;
    private String roleName;
    private OffsetDateTime createdAt;
}
