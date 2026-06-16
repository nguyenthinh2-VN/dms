package com.example.be.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class ReferredUserDto {
    private Long id;
    private String fullName;
    private String email;
    private String referralCode;
    private OffsetDateTime createdAt;
}
