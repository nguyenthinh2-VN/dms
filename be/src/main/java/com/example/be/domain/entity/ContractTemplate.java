package com.example.be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractTemplate {
    private Long id;
    private String code;
    private String name;
    private int version;
    
    @Builder.Default
    private TemplateStatus status = TemplateStatus.ACTIVE;
    
    private String originalFileName;
    private String storagePath;
    private String htmlContent;
    
    private Long createdBy;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    private List<TemplateField> fields;
}
