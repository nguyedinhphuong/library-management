package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;


@Getter
@Builder
@AllArgsConstructor
public class CategorySummaryResponse implements Serializable {

    private Integer id;
    private String code;
    private String name;
}
