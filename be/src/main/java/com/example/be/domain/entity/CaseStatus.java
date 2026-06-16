package com.example.be.domain.entity;

import com.example.be.application.util.LanguageContextHolder;

public enum CaseStatus {
    NEW("Mới", "新增"),
    PENDING_VERIFICATION("Đợi xác minh", "待驗證"),
    VERIFYING("Đang xác minh", "驗證中"),
    CONTRACT_NEGOTIATING("Đang chốt hợp đồng", "合約洽談中"),
    CONTRACTED("Đã chốt hợp đồng", "已簽約"),
    PROCESSING("Đang xử lý", "處理中"),
    PAUSED("Tạm dừng", "暫停"),
    ACCEPTANCE_PAYMENT("Đang nghiệm thu/thanh toán", "驗收/付款中"),
    CLOSED("Đã đóng", "已結案");

    private final String descriptionVi;
    private final String descriptionTw;

    CaseStatus(String descriptionVi, String descriptionTw) {
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
