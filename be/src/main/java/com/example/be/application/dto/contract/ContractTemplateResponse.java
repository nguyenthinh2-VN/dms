package com.example.be.application.dto;

import com.example.be.domain.entity.TemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractTemplateResponse {
    private Long id;
    private String code;
    private String name;
    private int version;
    private TemplateStatus status;
    private String originalFileName;
    private String htmlContent;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<TemplateFieldDto> fields;
}
