package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.TemplateFieldJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataTemplateFieldRepository extends JpaRepository<TemplateFieldJpaEntity, Long> {
    List<TemplateFieldJpaEntity> findByContractTemplateId(Long templateId);
    void deleteByContractTemplateId(Long templateId);
}
