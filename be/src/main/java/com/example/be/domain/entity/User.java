package com.example.be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String fullName;
    private String workEmail;
    private String position;
    private String phoneNumber;
    private String password;
    private String invitedByCode;
    private String personalReferralCode;
    private Role role;

    @Builder.Default
    private String status = "ACTIVE";
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
