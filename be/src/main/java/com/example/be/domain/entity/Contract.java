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
public class Contract {
    private Long id;
    private String contractNo;
    private Long templateId;
    private Integer templateVersion;
    private Long legalCaseId;
    private ContractStatus status;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
