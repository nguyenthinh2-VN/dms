package com.example.be.domain.repository;

import com.example.be.domain.entity.TemplateField;
import java.util.List;

public interface TemplateFieldRepository {
    List<TemplateField> findByTemplateId(Long templateId);
    TemplateField save(TemplateField field);
    void saveAll(List<TemplateField> fields);
    void deleteByTemplateId(Long templateId);
}
