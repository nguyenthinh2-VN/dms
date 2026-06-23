package com.example.be.domain.repository;

import com.example.be.domain.entity.LegalCase;
import java.util.List;
import java.util.Optional;

public interface LegalCaseRepository {
    LegalCase save(LegalCase legalCase);
    Optional<LegalCase> findById(Long id);
    List<LegalCase> findAll();
    List<LegalCase> findByRelatedUserId(Long userId);

    java.math.BigDecimal sumTotalCaseValue();
    java.math.BigDecimal sumTotalCaseValueByAssignedLawyerId(Long userId);
    Long countByAssignedLawyerId(Long userId);
    long count();
}
