package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.ContractJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataContractRepository extends JpaRepository<ContractJpaEntity, Long> {
    boolean existsByContractNo(String contractNo);

    Long countByCreatedBy(Long createdBy);
}
