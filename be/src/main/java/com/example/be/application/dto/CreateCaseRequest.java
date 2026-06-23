package com.example.be.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Loại vụ việc không được để trống")
    private String category;

    private String customCategory;

    private String description;
    private String referrerName;
    private String clientName;
    
    private String partnerName;
    private String internLawyerName;
    private String traineeName;
    
    @Min(value = 0, message = "Giá trị vụ việc phải lớn hơn hoặc bằng 0")
    private BigDecimal caseValue;
    
    @Builder.Default
    private Double referrerPercent = 0.0;
    @Builder.Default
    private Double assignedLawyerPercent = 0.0;
    @Builder.Default
    private Double partnerPercent = 0.0;
    @Builder.Default
    private Double internPercent = 0.0;
    @Builder.Default
    private Double traineePercent = 0.0;
}
