package com.project.library.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class DashboardStatsResponse implements Serializable {

    private Long totalStudents;
    private Long totalActiveStudents;
    private Long totalSuspendedStudents;
    private Long totalBooks;
    private Long totalAvailableBooks;
    private Long totalCategories;
    private Long currentBorrowing;
    private Long overdueBooks;
    private Long totalBorrowsThisMonth;
    private Long totalReturnsThisMonth;
    private Double borrowingRate;
    private LocalDateTime lastUpdated;
}
