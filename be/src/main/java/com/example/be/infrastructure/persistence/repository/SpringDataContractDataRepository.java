package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.ContractDataJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataContractDataRepository extends JpaRepository<ContractDataJpaEntity, Long> {
    Optional<ContractDataJpaEntity> findByContractId(Long contractId);
}
