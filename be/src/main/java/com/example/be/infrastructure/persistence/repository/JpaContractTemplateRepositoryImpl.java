package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.ContractTemplate;
import com.example.be.domain.entity.TemplateStatus;
import com.example.be.domain.repository.ContractTemplateRepository;
import com.example.be.infrastructure.persistence.entity.ContractTemplateJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaContractTemplateRepositoryImpl implements ContractTemplateRepository {

    private final SpringDataContractTemplateRepository repository;

    public JpaContractTemplateRepositoryImpl(SpringDataContractTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public ContractTemplate save(ContractTemplate template) {
        ContractTemplateJpaEntity entity = toJpaEntity(template);
        ContractTemplateJpaEntity savedEntity = repository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public Optional<ContractTemplate> findById(Long id) {
        return repository.findById(id).map(this::toDomainEntity);
    }

    @Override
    public List<ContractTemplate> findByStatus(TemplateStatus status) {
        return repository.findByStatus(status).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ContractTemplate> findByCodeAndVersion(String code, int version) {
        return repository.findByCodeAndVersion(code, version).map(this::toDomainEntity);
    }

    @Override
    public Optional<ContractTemplate> findActiveByCode(String code) {
        return repository.findByCodeAndStatus(code, TemplateStatus.ACTIVE).map(this::toDomainEntity);
    }

    private ContractTemplateJpaEntity toJpaEntity(ContractTemplate domain) {
        if (domain == null) return null;
        return ContractTemplateJpaEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .version(domain.getVersion())
                .status(domain.getStatus())
                .originalFileName(domain.getOriginalFileName())
                .storagePath(domain.getStoragePath())
                .htmlContent(domain.getHtmlContent())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private ContractTemplate toDomainEntity(ContractTemplateJpaEntity entity) {
        if (entity == null) return null;
        return ContractTemplate.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .version(entity.getVersion())
                .status(entity.getStatus())
                .originalFileName(entity.getOriginalFileName())
                .storagePath(entity.getStoragePath())
                .htmlContent(entity.getHtmlContent())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
