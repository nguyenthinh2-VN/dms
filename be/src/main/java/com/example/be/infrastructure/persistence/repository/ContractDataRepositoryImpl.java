package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.ContractData;
import com.example.be.domain.repository.ContractDataRepository;
import com.example.be.infrastructure.persistence.entity.ContractDataJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContractDataRepositoryImpl implements ContractDataRepository {

    private final SpringDataContractDataRepository springDataRepository;
    private final SpringDataContractRepository contractRepository;

    public ContractDataRepositoryImpl(SpringDataContractDataRepository springDataRepository,
                                      SpringDataContractRepository contractRepository) {
        this.springDataRepository = springDataRepository;
        this.contractRepository = contractRepository;
    }

    @Override
    public ContractData save(ContractData contractData) {
        ContractDataJpaEntity jpaEntity = mapToJpaEntity(contractData);
        ContractDataJpaEntity saved = springDataRepository.save(jpaEntity);
        return mapToDomainEntity(saved);
    }

    @Override
    public Optional<ContractData> findByContractId(Long contractId) {
        return springDataRepository.findByContractId(contractId).map(this::mapToDomainEntity);
    }

    private ContractDataJpaEntity mapToJpaEntity(ContractData domain) {
        if (domain == null) return null;
        ContractDataJpaEntity jpa = new ContractDataJpaEntity();
        jpa.setId(domain.getId());
        if (domain.getContractId() != null) {
            jpa.setContract(contractRepository.getReferenceById(domain.getContractId()));
        }
        jpa.setDataJson(domain.getDataJson());
        return jpa;
    }

    private ContractData mapToDomainEntity(ContractDataJpaEntity jpa) {
        if (jpa == null) return null;
        ContractData domain = new ContractData();
        domain.setId(jpa.getId());
        if (jpa.getContract() != null) {
            domain.setContractId(jpa.getContract().getId());
        }
        domain.setDataJson(jpa.getDataJson());
        return domain;
    }
}
