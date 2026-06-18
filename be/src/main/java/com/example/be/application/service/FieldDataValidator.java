package com.example.be.application.service;

import com.example.be.domain.entity.TemplateField;
import com.example.be.domain.entity.FieldType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
public class FieldDataValidator {

    public void validate(List<TemplateField> schemaFields, Map<String, String> data) {
        for (TemplateField field : schemaFields) {
            String value = data.get(field.getFieldKey());
            
            if (field.isRequired()) {
                if (value == null || value.trim().isEmpty()) {
                    throw new IllegalArgumentException("Field " + field.getLabel() + " bắt buộc");
                }
            }

            if (value != null && !value.trim().isEmpty()) {
                switch (field.getFieldType()) {
                    case TEXT:
                        if (value.length() > 1000) throw new IllegalArgumentException("Field " + field.getLabel() + " quá dài (tối đa 1000 ký tự)");
                        break;
                    case PARAGRAPH:
                        if (value.length() > 10000) throw new IllegalArgumentException("Field " + field.getLabel() + " quá dài (tối đa 10000 ký tự)");
                        break;
                    case NUMBER:
                    case MONEY:
                        try {
                            java.math.BigDecimal val = new java.math.BigDecimal(value);
                            if (field.getFieldType() == FieldType.MONEY && val.compareTo(java.math.BigDecimal.ZERO) < 0) {
                                throw new IllegalArgumentException("Field " + field.getLabel() + " phải >= 0");
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Field " + field.getLabel() + " phải là số");
                        }
                        break;
                    case DATE:
                        try {
                            LocalDate.parse(value);
                        } catch (DateTimeParseException e) {
                            throw new IllegalArgumentException("Field " + field.getLabel() + " sai định dạng ngày (yyyy-MM-dd)");
                        }
                        break;
                }
            }
        }
    }
}
