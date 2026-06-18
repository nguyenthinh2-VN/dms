package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.ContractSnapshot;
import com.example.be.domain.repository.ContractSnapshotRepository;
import com.example.be.infrastructure.persistence.entity.ContractSnapshotJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContractSnapshotRepositoryImpl implements ContractSnapshotRepository {

    private final SpringDataContractSnapshotRepository springDataRepository;
    private final SpringDataContractRepository contractRepository;

    public ContractSnapshotRepositoryImpl(SpringDataContractSnapshotRepository springDataRepository,
                                          SpringDataContractRepository contractRepository) {
        this.springDataRepository = springDataRepository;
        this.contractRepository = contractRepository;
    }

    @Override
    public ContractSnapshot save(ContractSnapshot contractSnapshot) {
        ContractSnapshotJpaEntity jpaEntity = mapToJpaEntity(contractSnapshot);
        ContractSnapshotJpaEntity saved = springDataRepository.save(jpaEntity);
        return mapToDomainEntity(saved);
    }

    @Override
    public Optional<ContractSnapshot> findByContractId(Long contractId) {
        return springDataRepository.findByContractId(contractId).map(this::mapToDomainEntity);
    }

    private ContractSnapshotJpaEntity mapToJpaEntity(ContractSnapshot domain) {
        if (domain == null) return null;
        ContractSnapshotJpaEntity jpa = new ContractSnapshotJpaEntity();
        jpa.setId(domain.getId());
        if (domain.getContractId() != null) {
            jpa.setContract(contractRepository.getReferenceById(domain.getContractId()));
        }
        jpa.setRenderedHtml(domain.getRenderedHtml());
        jpa.setDocxPath(domain.getDocxPath());
        jpa.setPdfPath(domain.getPdfPath());
        jpa.setCreatedAt(domain.getCreatedAt());
        return jpa;
    }

    private ContractSnapshot mapToDomainEntity(ContractSnapshotJpaEntity jpa) {
        if (jpa == null) return null;
        ContractSnapshot domain = new ContractSnapshot();
        domain.setId(jpa.getId());
        if (jpa.getContract() != null) {
            domain.setContractId(jpa.getContract().getId());
        }
        domain.setRenderedHtml(jpa.getRenderedHtml());
        domain.setDocxPath(jpa.getDocxPath());
        domain.setPdfPath(jpa.getPdfPath());
        domain.setCreatedAt(jpa.getCreatedAt());
        return domain;
    }
}
