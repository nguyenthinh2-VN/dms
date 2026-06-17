package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.TemplateField;
import com.example.be.domain.repository.TemplateFieldRepository;
import com.example.be.infrastructure.persistence.entity.ContractTemplateJpaEntity;
import com.example.be.infrastructure.persistence.entity.TemplateFieldJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JpaTemplateFieldRepositoryImpl implements TemplateFieldRepository {

    private final SpringDataTemplateFieldRepository repository;

    public JpaTemplateFieldRepositoryImpl(SpringDataTemplateFieldRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TemplateField> findByTemplateId(Long templateId) {
        return repository.findByContractTemplateId(templateId).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public TemplateField save(TemplateField field) {
        TemplateFieldJpaEntity entity = toJpaEntity(field);
        TemplateFieldJpaEntity savedEntity = repository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public void saveAll(List<TemplateField> fields) {
        List<TemplateFieldJpaEntity> entities = fields.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }

    @Override
    public void deleteByTemplateId(Long templateId) {
        repository.deleteByContractTemplateId(templateId);
    }

    private TemplateFieldJpaEntity toJpaEntity(TemplateField domain) {
        if (domain == null) return null;
        ContractTemplateJpaEntity contractTemplate = null;
        if (domain.getTemplateId() != null) {
            contractTemplate = new ContractTemplateJpaEntity();
            contractTemplate.setId(domain.getTemplateId());
        }

        return TemplateFieldJpaEntity.builder()
                .id(domain.getId())
                .contractTemplate(contractTemplate)
                .fieldKey(domain.getFieldKey())
                .label(domain.getLabel())
                .fieldType(domain.getFieldType())
                .required(domain.isRequired())
                .displayOrder(domain.getDisplayOrder())
                .defaultValue(domain.getDefaultValue())
                .build();
    }

    private TemplateField toDomainEntity(TemplateFieldJpaEntity entity) {
        if (entity == null) return null;
        return TemplateField.builder()
                .id(entity.getId())
                .templateId(entity.getContractTemplate() != null ? entity.getContractTemplate().getId() : null)
                .fieldKey(entity.getFieldKey())
                .label(entity.getLabel())
                .fieldType(entity.getFieldType())
                .required(entity.isRequired())
                .displayOrder(entity.getDisplayOrder())
                .defaultValue(entity.getDefaultValue())
                .build();
    }
}
