package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.LegalCaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataLegalCaseRepository extends JpaRepository<LegalCaseJpaEntity, Long> {

    @Query("SELECT DISTINCT lc FROM LegalCaseJpaEntity lc " +
           "LEFT JOIN CaseAssignmentJpaEntity ca ON ca.legalCase.id = lc.id " +
           "WHERE lc.assignedLawyer.id = :userId OR ca.assignee.id = :userId")
    List<LegalCaseJpaEntity> findByRelatedUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(lc.caseValue) FROM LegalCaseJpaEntity lc")
    java.math.BigDecimal sumTotalCaseValue();

    @Query("SELECT SUM(lc.caseValue) FROM LegalCaseJpaEntity lc WHERE lc.assignedLawyer.id = :userId")
    java.math.BigDecimal sumTotalCaseValueByAssignedLawyerId(@Param("userId") Long userId);

    Long countByAssignedLawyerId(Long userId);
}
