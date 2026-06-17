package com.example.be.domain.repository;

import com.example.be.domain.entity.ContractTemplate;
import com.example.be.domain.entity.TemplateStatus;

import java.util.List;
import java.util.Optional;

public interface ContractTemplateRepository {
    ContractTemplate save(ContractTemplate template);
    Optional<ContractTemplate> findById(Long id);
    List<ContractTemplate> findByStatus(TemplateStatus status);
    Optional<ContractTemplate> findByCodeAndVersion(String code, int version);
    Optional<ContractTemplate> findActiveByCode(String code);
}
