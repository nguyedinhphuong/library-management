package com.project.library.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Major {

    @JsonProperty("information_technology")
    INFORMATION_TECHNOLOGY,   // Công nghệ thông tin

    @JsonProperty("computer_science")
    COMPUTER_SCIENCE,         // Khoa học máy tính

    @JsonProperty("software_engineering")
    SOFTWARE_ENGINEERING,     // Kỹ thuật phần mềm

    @JsonProperty("information_systems")
    INFORMATION_SYSTEMS,      // Hệ thống thông tin

    @JsonProperty("data_science")
    DATA_SCIENCE,             // Khoa học dữ liệu

    @JsonProperty("cyber_security")
    CYBER_SECURITY,           // An toàn thông tin

    @JsonProperty("artificial_intelligence")
    ARTIFICIAL_INTELLIGENCE,  // Trí tuệ nhân tạo

    @JsonProperty("business_administration")
    BUSINESS_ADMINISTRATION, // Quản trị kinh doanh

    @JsonProperty("accounting")
    ACCOUNTING,               // Kế toán

    @JsonProperty("finance")
    FINANCE,                  // Tài chính – Ngân hàng

    @JsonProperty("marketing")
    MARKETING,                // Marketing

    @JsonProperty("economics")
    ECONOMICS                 // Kinh tế
}
