package com.example.be.domain.entity;

import com.example.be.application.util.MessageUtils;

public enum FieldType {
    TEXT("Văn bản"),
    NUMBER("Số"),
    DATE("Ngày"),
    MONEY("Tiền tệ"),
    PARAGRAPH("Đoạn văn");

    private final String description;

    FieldType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return MessageUtils.getMessage("FIELD_TYPE_" + this.name(), this.description);
    }
}
