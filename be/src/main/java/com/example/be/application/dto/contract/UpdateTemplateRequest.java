package com.example.be.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTemplateRequest {
    @NotBlank(message = "Tên mẫu hợp đồng không được để trống")
    private String name;
    
    @NotEmpty(message = "Danh sách trường không được để trống")
    private List<TemplateFieldDto> fields;
}
