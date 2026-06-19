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
public class CaseAssignment {
    private Long id;
    private LegalCase legalCase;
    private User assignee;
    private User assigner;
    private String roleInCase;
    private String note;
    private Double commissionPercent;
    private OffsetDateTime createdAt;
}
