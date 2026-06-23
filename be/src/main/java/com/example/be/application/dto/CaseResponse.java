package com.example.be.application.dto;

import com.example.be.domain.entity.CaseStatus;
import com.example.be.domain.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseResponse {
    private Long id;
    private String generatedTitle;
    private String title;
    private String category;
    private String customCategory;
    private String description;
    private String referrerName;
    private String clientName;
    
    private StaffUserDto assignedLawyer;
    private String partnerName;
    private String internLawyerName;
    private String traineeName;
    
    private Long createdBy;
    private String creatorName;
    
    private BigDecimal caseValue;
    private PaymentStatus paymentStatus;
    private CaseStatus status;
    
    private Double referrerPercent;
    private Double assignedLawyerPercent;
    private Double partnerPercent;
    private Double internPercent;
    private Double traineePercent;
    
    private BigDecimal referrerValue;
    private BigDecimal assignedLawyerValue;
    private BigDecimal partnerValue;
    private BigDecimal internValue;
    private BigDecimal traineeValue;
    private BigDecimal netValue;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
