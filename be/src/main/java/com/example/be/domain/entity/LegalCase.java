package com.example.be.domain.entity;


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
public class LegalCase {

    private Long id;
    private String generatedTitle;
    private String title;

    private CaseCategory category;


    private String customCategory;

    private String description;
    private String referrerName;
    
    private User assignedLawyer;
    private String partnerName;
    private String internLawyerName;
    private String traineeName;
    
    private Long createdBy;
    private String creatorName;
    
    private BigDecimal caseValue;
    
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    
    @Builder.Default
    private CaseStatus status = CaseStatus.NEW;
    
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

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
