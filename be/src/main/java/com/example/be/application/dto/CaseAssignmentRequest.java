package com.example.be.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAssignmentRequest {
    
    @NotNull(message = "ID nhân sự không được để trống")
    private Long assigneeId;
    
    @NotNull(message = "Vai trò không được để trống")
    private String roleInCase; // LAWYER, PARTNER, INTERN_LAWYER, TRAINEE
    
    private String note;
    
    @NotNull(message = "Phần trăm hoa hồng không được để trống")
    private Double commissionPercent;
}
