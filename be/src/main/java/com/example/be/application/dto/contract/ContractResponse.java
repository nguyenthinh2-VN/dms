package com.example.be.application.dto.contract;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
public class ContractResponse {
    private Long id;
    private String contractNo;
    private Long templateId;
    private Integer templateVersion;
    private Long legalCaseId;
    private String status;
    private ContractDownloadUrlDto downloadUrl;
    private String renderedHtml;
    private Map<String, String> data;
    private Long createdBy;
    private OffsetDateTime createdAt;
}
