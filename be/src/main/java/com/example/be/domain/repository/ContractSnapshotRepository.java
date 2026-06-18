package com.example.be.domain.repository;

import com.example.be.domain.entity.ContractSnapshot;
import java.util.Optional;

public interface ContractSnapshotRepository {
    ContractSnapshot save(ContractSnapshot contractSnapshot);
    Optional<ContractSnapshot> findByContractId(Long contractId);
}
