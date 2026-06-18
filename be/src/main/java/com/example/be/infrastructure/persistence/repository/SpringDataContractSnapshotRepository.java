package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.ContractSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataContractSnapshotRepository extends JpaRepository<ContractSnapshotJpaEntity, Long> {
    Optional<ContractSnapshotJpaEntity> findByContractId(Long contractId);
}
