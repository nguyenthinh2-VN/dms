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
public class Reminder {
    private Long id;
    private User user;
    private LegalCase legalCase;
    private OffsetDateTime deadline;
    private String note;
    @Builder.Default
    private boolean isCompleted = false;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
