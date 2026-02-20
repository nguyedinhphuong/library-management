package com.spring.code.demo.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TokenType {
    @JsonProperty("access_token")
    ACCESS_TOKEN,

    @JsonProperty("refresh_token")
    REFRESH_TOKEN,

    @JsonProperty("reset_token")
    RESET_TOKEN
}
