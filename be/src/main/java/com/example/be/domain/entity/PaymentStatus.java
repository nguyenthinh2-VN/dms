package com.example.be.domain.entity;

import com.example.be.application.util.LanguageContextHolder;

public enum PaymentStatus {
    UNPAID("Chưa thu", "未付款"),
    PAID("Đã thu", "已付款");

    private final String descriptionVi;
    private final String descriptionTw;

    PaymentStatus(String descriptionVi, String descriptionTw) {
        this.descriptionVi = descriptionVi;
        this.descriptionTw = descriptionTw;
    }

    public String getDescription() {
        String lan = LanguageContextHolder.getLanguage();
        if ("TW".equalsIgnoreCase(lan)) {
            return descriptionTw;
        }
        return descriptionVi;
    }
}
