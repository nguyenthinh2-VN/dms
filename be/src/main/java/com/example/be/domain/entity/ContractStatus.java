package com.example.be.domain.entity;

public enum ContractStatus {
    DRAFT("Nháp"),
    FINALIZED("Đã chốt");

    private final String description;

    ContractStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
