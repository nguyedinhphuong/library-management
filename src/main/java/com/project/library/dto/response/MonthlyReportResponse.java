package com.project.library.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MonthlyReportResponse {

    private String months;
    private Long totalBorrows;
    private Long totalReturns;
    private Long newStudents;
    private Long newBooks;
    private List<MostBorrowedBookResponse> topBorrowedBooks;
    private List<StudentRankingResponse> mostActiveStudents;
    private Double overdueRate;
    private Long currentOverdue;
    private Long totalActiveStudents;
    private Long totalAvailableBooks;
}
