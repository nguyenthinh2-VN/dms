package com.example.be.application.dto.contract;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class ContractListItemResponse {
    private Long id;
    private String contractNo;
    private Long templateId;
    private String templateName;
    private Long legalCaseId;
    private String status;
    private Long createdBy;
    private String creatorName;
    private OffsetDateTime createdAt;
}
