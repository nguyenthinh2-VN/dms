package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.TemplateStatus;
import com.example.be.infrastructure.persistence.entity.ContractTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataContractTemplateRepository extends JpaRepository<ContractTemplateJpaEntity, Long> {
    List<ContractTemplateJpaEntity> findByStatus(TemplateStatus status);
    Optional<ContractTemplateJpaEntity> findByCodeAndVersion(String code, int version);
    Optional<ContractTemplateJpaEntity> findByCodeAndStatus(String code, TemplateStatus status);
}
