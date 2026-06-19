package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.CaseAssignmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataCaseAssignmentRepository extends JpaRepository<CaseAssignmentJpaEntity, Long> {
    List<CaseAssignmentJpaEntity> findByLegalCase_IdOrderByCreatedAtDesc(Long legalCaseId);
}
