package com.example.be.infrastructure.persistence.entity;

import com.example.be.domain.entity.CaseCategory;

import com.example.be.domain.entity.CaseStatus;
import com.example.be.domain.entity.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "legal_cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalCaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String generatedTitle;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_category")
    private CaseCategory category;

    @Column(name = "custom_category")
    private String customCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String referrerName;

    @Column(name = "client_name")
    private String clientName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_lawyer_id")
    private UserJpaEntity assignedLawyer;

    @Column(name = "partner_name")
    private String partnerName;

    @Column(name = "intern_lawyer_name")
    private String internLawyerName;

    @Column(name = "trainee_name")
    private String traineeName;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(nullable = false)
    private BigDecimal caseValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedAt;
}
