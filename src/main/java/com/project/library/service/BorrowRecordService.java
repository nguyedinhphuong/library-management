package com.project.library.service;

import com.project.library.dto.request.borrow.CreateBorrowRequest;
import com.project.library.dto.request.borrow.RenewBorrowRequest;
import com.project.library.dto.request.borrow.ReportLostRequest;
import com.project.library.dto.request.borrow.ReturnBookRequest;
import com.project.library.dto.response.BorrowRecordResponse;
import com.project.library.dto.response.PageResponse;
import com.project.library.utils.BorrowStatus;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRecordService {

    BorrowRecordResponse borrowBook(CreateBorrowRequest request);

    BorrowRecordResponse getById(Long id);

    BorrowRecordResponse returnBook(Long id, ReturnBookRequest request);

    List<BorrowRecordResponse> getOverdueRecords();


    PageResponse<?> searchBorrowRecords(Long studentId, Long bookId, BorrowStatus status,
                                        LocalDate fromDate, LocalDate toDate,
                                        int pageNo, int pageSize, String sortBy);

    BorrowRecordResponse renewBorrow(Long id, RenewBorrowRequest request);
    List<BorrowRecordResponse> getDueSoonRecords(int days);
    BorrowRecordResponse reportLost(Long id, ReportLostRequest request);
}
