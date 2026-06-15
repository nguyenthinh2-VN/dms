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
public class Role {
    private Long id;
    private String code;
    private String name;
    private String description;
    
    @Builder.Default
    private String status = "ACTIVE";
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
