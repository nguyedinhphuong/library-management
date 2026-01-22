package com.project.library.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BorrowStatus {

    @JsonProperty("borrowing")
    BORROWING,     // Đang mượn

    @JsonProperty("returned")
    RETURNED,      // Đã trả

    @JsonProperty("overdue")
    OVERDUE        // Quá hạn
}
