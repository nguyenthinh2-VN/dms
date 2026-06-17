package com.example.be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateField {
    private Long id;
    private Long templateId;
    private String fieldKey;
    private String label;
    private FieldType fieldType;
    
    @Builder.Default
    private boolean required = true;
    
    private int displayOrder;
    private String defaultValue;
}
