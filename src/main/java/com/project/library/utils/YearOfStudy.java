package com.project.library.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum YearOfStudy {

    @JsonProperty("year_1")
    YEAR_1,

    @JsonProperty("year_2")
    YEAR_2,

    @JsonProperty("year_3")
    YEAR_3,

    @JsonProperty("year_4")
    YEAR_4,

    @JsonProperty("graduated")
    GRADUATED
}
