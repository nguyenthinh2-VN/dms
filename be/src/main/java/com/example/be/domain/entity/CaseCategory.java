package com.example.be.domain.entity;

public enum CaseCategory {
    CIVIL("Dân sự"),
    CRIMINAL("Hình sự"),
    ADMINISTRATIVE("Hành chính"),
    MARRIAGE_FAMILY("Hôn nhân & Gia đình"),
    LAND_REAL_ESTATE("Đất đai – Bất động sản"),
    LABOR("Lao động"),
    CORPORATE_INVESTMENT("Doanh nghiệp – Đầu tư"),
    COMMERCIAL("Kinh doanh – Thương mại"),
    INTELLECTUAL_PROPERTY("Sở hữu trí tuệ"),
    TAX_FINANCE("Thuế – Tài chính"),
    BANKRUPTCY("Phá sản"),
    OTHER("Khác");

    private final String description;

    CaseCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
