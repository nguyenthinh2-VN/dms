package com.example.be.domain.entity;

public enum PaymentStatus {
    UNPAID("Chưa thu"),
    PAID("Đã thu");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
