package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.Contract;
import com.example.be.domain.repository.ContractRepository;
import com.example.be.infrastructure.persistence.entity.ContractJpaEntity;
import com.example.be.infrastructure.persistence.entity.ContractTemplateJpaEntity;
import com.example.be.infrastructure.persistence.entity.LegalCaseJpaEntity;
import com.example.be.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContractRepositoryImpl implements ContractRepository {

    private final SpringDataContractRepository springDataRepository;
    private final SpringDataContractTemplateRepository templateRepository;
    private final SpringDataLegalCaseRepository legalCaseRepository;
    private final SpringDataUserRepository userRepository;

    public ContractRepositoryImpl(SpringDataContractRepository springDataRepository,
                                  SpringDataContractTemplateRepository templateRepository,
                                  SpringDataLegalCaseRepository legalCaseRepository,
                                  SpringDataUserRepository userRepository) {
        this.springDataRepository = springDataRepository;
        this.templateRepository = templateRepository;
        this.legalCaseRepository = legalCaseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Contract save(Contract contract) {
        ContractJpaEntity jpaEntity = mapToJpaEntity(contract);
        ContractJpaEntity saved = springDataRepository.save(jpaEntity);
        return mapToDomainEntity(saved);
    }

    @Override
    public Optional<Contract> findById(Long id) {
        return springDataRepository.findById(id).map(this::mapToDomainEntity);
    }

    @Override
    public boolean existsByContractNo(String contractNo) {
        return springDataRepository.existsByContractNo(contractNo);
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }

    @Override
    public Long countByCreatedBy(Long createdBy) {
        return springDataRepository.countByCreatedBy(createdBy);
    }

    private ContractJpaEntity mapToJpaEntity(Contract domain) {
        if (domain == null) return null;
        ContractJpaEntity jpa = new ContractJpaEntity();
        jpa.setId(domain.getId());
        jpa.setContractNo(domain.getContractNo());
        
        if (domain.getTemplateId() != null) {
            jpa.setTemplate(templateRepository.getReferenceById(domain.getTemplateId()));
        }
        jpa.setTemplateVersion(domain.getTemplateVersion());

        if (domain.getLegalCaseId() != null) {
            jpa.setLegalCase(legalCaseRepository.getReferenceById(domain.getLegalCaseId()));
        }
        
        jpa.setStatus(domain.getStatus());
        
        if (domain.getCreatedBy() != null) {
            jpa.setCreatedBy(userRepository.getReferenceById(domain.getCreatedBy()));
        }
        
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        return jpa;
    }

    private Contract mapToDomainEntity(ContractJpaEntity jpa) {
        if (jpa == null) return null;
        Contract domain = new Contract();
        domain.setId(jpa.getId());
        domain.setContractNo(jpa.getContractNo());
        
        if (jpa.getTemplate() != null) {
            domain.setTemplateId(jpa.getTemplate().getId());
        }
        domain.setTemplateVersion(jpa.getTemplateVersion());
        
        if (jpa.getLegalCase() != null) {
            domain.setLegalCaseId(jpa.getLegalCase().getId());
        }
        
        domain.setStatus(jpa.getStatus());
        
        if (jpa.getCreatedBy() != null) {
            domain.setCreatedBy(jpa.getCreatedBy().getId());
        }
        
        domain.setCreatedAt(jpa.getCreatedAt());
        domain.setUpdatedAt(jpa.getUpdatedAt());
        return domain;
    }
}
