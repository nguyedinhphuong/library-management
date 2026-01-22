package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class BookSummaryResponse implements Serializable {
    private Long id;
    private String title;
    private String author;
    private String isbn;
}