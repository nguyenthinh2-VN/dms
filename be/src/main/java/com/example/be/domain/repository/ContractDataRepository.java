package com.example.be.domain.repository;

import com.example.be.domain.entity.ContractData;
import java.util.Optional;

public interface ContractDataRepository {
    ContractData save(ContractData contractData);
    Optional<ContractData> findByContractId(Long contractId);
}
