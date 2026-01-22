package com.project.library.dto.response;

import com.project.library.utils.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BorrowRecordResponse implements Serializable {

    private Long id;
    private StudentSummaryResponse student;
    private BookSummaryResponse book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;
    private Long daysRemaining ;
    private Boolean isOverdue;
    private Long daysOverdue;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
