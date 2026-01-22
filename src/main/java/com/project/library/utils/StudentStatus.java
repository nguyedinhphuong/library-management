package com.project.library.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StudentStatus {

    @JsonProperty("active")
    ACTIVE,        // Đang học, cho phép mượn sách

    @JsonProperty("suspended")
    SUSPENDED      // Bị đình chỉ, không cho mượn
}
