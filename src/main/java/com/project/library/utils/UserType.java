package com.project.library.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserType {

    @JsonProperty("student")
    STUDENT,       // Sinh viên

    @JsonProperty("librarian")
    LIBRARIAN,     // Thủ thư

    @JsonProperty("admin")
    ADMIN          // Quản trị viên
}
