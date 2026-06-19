package com.example.be.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAssignmentResponse {
    private Long id;
    private Long legalCaseId;
    private StaffUserDto assignee;
    private StaffUserDto assigner;
    private String roleInCase;
    private String note;
    private Double commissionPercent;
    private OffsetDateTime createdAt;
}
