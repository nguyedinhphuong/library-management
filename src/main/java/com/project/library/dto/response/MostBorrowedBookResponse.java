package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class MostBorrowedBookResponse implements Serializable {
    private Integer rank;
    private BookSummaryResponse book;
    private Long totalBorrowCount;
    private Long currentBorrowingCount;
}
