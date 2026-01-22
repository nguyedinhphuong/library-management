package com.project.library.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserStatus {

    @JsonProperty("active")
    ACTIVE,        // Tài khoản hoạt động

    @JsonProperty("inactive")
    INACTIVE       // Tài khoản bị vô hiệu hóa
}
