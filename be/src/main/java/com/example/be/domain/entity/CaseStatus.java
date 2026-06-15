package com.example.be.domain.entity;

public enum CaseStatus {
    NEW("Mới"),
    WAITING_VERIFICATION("Đợi xác minh"),
    VERIFYING("Đang xác minh"),
    NEGOTIATING("Đang chốt hợp đồng"),
    CONTRACTED("Đã chốt hợp đồng"),
    PROCESSING("Đang xử lý"),
    PAUSED("Tạm dừng"),
    ACCEPTANCE_PAYMENT("Đang nghiệm thu/thanh toán"),
    CLOSED("Đã đóng");

    private final String description;

    CaseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
