package com.example.be.application.dto;

import com.example.be.domain.entity.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFieldDto {
    private String fieldKey;
    private String label;
    private FieldType fieldType;
    private boolean required;
    private int displayOrder;
    private String defaultValue;
}
