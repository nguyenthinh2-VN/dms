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
public class Rule {
    private Long id;
    private Long userId;
    private Long permissionId;
    
    @Builder.Default
    private String status = "GRANTED";
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
