package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class BookUsageResponse implements Serializable {

    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private Long totalBorrowCount;
    private Long lastSixMonthsBorrowCount;
    private Double usageRate;
    private String recommendation;  // "KEEP", "MONITOR", "ARCHIVE"
}
