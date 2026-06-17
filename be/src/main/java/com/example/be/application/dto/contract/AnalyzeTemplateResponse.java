package com.example.be.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeTemplateResponse {
    private String previewHtml;
    private List<TemplateFieldDto> fields;
    private List<String> warnings;
}
