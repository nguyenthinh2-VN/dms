package com.example.be.domain.entity;

import com.example.be.application.util.LanguageContextHolder;

public enum CaseCategory {
    CIVIL("Dân sự", "民事"),
    CRIMINAL("Hình sự", "刑事"),
    ADMINISTRATIVE("Hành chính", "行政"),
    MARRIAGE_FAMILY("Hôn nhân & Gia đình", "婚姻與家庭"),
    LAND_REAL_ESTATE("Đất đai – Bất động sản", "土地與房地產"),
    LABOR("Lao động", "勞動"),
    CORPORATE_INVESTMENT("Doanh nghiệp – Đầu tư", "企業與投資"),
    COMMERCIAL("Kinh doanh – Thương mại", "商業與貿易"),
    INTELLECTUAL_PROPERTY("Sở hữu trí tuệ", "智慧財產權"),
    TAX_FINANCE("Thuế – Tài chính", "稅務與財務"),
    BANKRUPTCY("Phá sản", "破產"),
    OTHER("Khác", "其他");

    private final String descriptionVi;
    private final String descriptionTw;

    CaseCategory(String descriptionVi, String descriptionTw) {
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
