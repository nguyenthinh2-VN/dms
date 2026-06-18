package com.example.be.domain.repository;

import com.example.be.domain.entity.Contract;
import java.util.Optional;

public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(Long id);
    boolean existsByContractNo(String contractNo);
}
