package com.project.library.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BookStatus {

    @JsonProperty("available")
    AVAILABLE,     // Sẵn sàng cho mượn

    @JsonProperty("maintenance")
    MAINTENANCE,   // Đang bảo trì

    @JsonProperty("lost")
    LOST,          // Bị mất

    @JsonProperty("damaged")
    DAMAGED,       // Bị hư hỏng

    @JsonProperty("archived")
    ARCHIVED       // Lưu trữ (không cho mượn nữa)
}
