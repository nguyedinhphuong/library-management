package com.project.library.converter;

import com.project.library.dto.request.borrow.CreateBorrowRequest;
import com.project.library.dto.response.BookSummaryResponse;
import com.project.library.dto.response.BorrowRecordResponse;
import com.project.library.dto.response.StudentSummaryResponse;
import com.project.library.model.Book;
import com.project.library.model.BorrowRecord;
import com.project.library.model.Student;
import com.project.library.utils.BorrowStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BorrowRecordMapper {

    public BorrowRecordMapper() {
    }

    public static BorrowRecord toEntity(CreateBorrowRequest request, Student student, Book book) {
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14); // 2 tuan
        return BorrowRecord.builder()
                .student(student)
                .book(book)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowStatus.BORROWING)
                .notes(request.getNotes())
                .build();
    }

    public static BorrowRecordResponse toResponse(BorrowRecord record){
        // calculate days remaining
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, record.getDueDate());
        boolean isOverdue = record.getReturnDate() == null && today.isAfter(record.getDueDate());

        Long daysOverdue = isOverdue ? ChronoUnit.DAYS.between(record.getDueDate(), today) : null ;

        // map student
        StudentSummaryResponse studentResponse = StudentSummaryResponse.builder()
                .id(record.getStudent().getId())
                .studentCode(record.getStudent().getStudentCode())
                .fullName(record.getStudent().getFullName())
                .email(record.getStudent().getEmail())
                .phone(record.getStudent().getPhone())
                .build();
        // map book
        BookSummaryResponse bookResponse = BookMapper.toSummaryResponse(record.getBook());

        return BorrowRecordResponse.builder()
                .id(record.getId())
                .student(studentResponse)
                .book(bookResponse)
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .daysRemaining(daysRemaining)
                .isOverdue(isOverdue)
                .daysOverdue(daysOverdue)
                .notes(record.getNotes())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
