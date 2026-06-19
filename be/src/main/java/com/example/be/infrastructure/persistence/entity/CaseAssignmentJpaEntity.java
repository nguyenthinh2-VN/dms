package com.example.be.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "case_assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAssignmentJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private LegalCaseJpaEntity legalCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private UserJpaEntity assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id", nullable = false)
    private UserJpaEntity assigner;

    @Column(name = "role_in_case", nullable = false)
    private String roleInCase;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "commission_percent")
    private Double commissionPercent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
