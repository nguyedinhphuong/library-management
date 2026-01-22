package com.project.library.controller;


import com.project.library.dto.request.borrow.CreateBorrowRequest;
import com.project.library.dto.request.borrow.ReturnBookRequest;
import com.project.library.dto.response.BorrowRecordResponse;
import com.project.library.dto.response.PageResponse;
import com.project.library.dto.response.ResponseData;
import com.project.library.exception.BusinessException;
import com.project.library.service.BorrowRecordService;
import com.project.library.utils.BorrowStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/borrows")
@Slf4j
@Tag(name = "Borrow Record Controller")
@Validated
@RequiredArgsConstructor
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;

    @Operation(summary = "Borrow Book", description = "Student borrows a book")
    @PostMapping
    public ResponseEntity<ResponseData<BorrowRecordResponse>> borrowBook(
            @Valid @RequestBody CreateBorrowRequest request) {
        try {
            log.info("API borrow book called - studentId: {}, bookId: {}",
                    request.getStudentId(), request.getBookId());
            BorrowRecordResponse response = borrowRecordService.borrowBook(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseData<>(HttpStatus.CREATED.value(), "Borrow book successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when borrowing book, message: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when borrowing book", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
    @Operation(summary = "Get Borrow Record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<BorrowRecordResponse>> getBorrowRecordById(@PathVariable Long id) {
        try {
            log.debug("API get borrow record by id called, id: {}", id);
            BorrowRecordResponse response = borrowRecordService.getById(id);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Get borrow record successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when getting borrow record, id: {}, message: {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when getting borrow record, id: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Return Book", description = "Student returns a borrowed book")
    @PostMapping("/{id}/return")
    public ResponseEntity<ResponseData<BorrowRecordResponse>> returnBook(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ReturnBookRequest request) {
        try {
            log.info("API return book called, borrowRecordId: {}", id);
            ReturnBookRequest req = request != null ? request : new ReturnBookRequest();
            BorrowRecordResponse response = borrowRecordService.returnBook(id, req);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Return book successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when returning book, id: {}, message: {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when returning book, id: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Get Overdue Records", description = "Get all overdue borrow records")
    @GetMapping("/overdue")
    public ResponseEntity<ResponseData<List<BorrowRecordResponse>>> getOverdueRecords() {
        try {
            log.debug("API get overdue records called");
            List<BorrowRecordResponse> response = borrowRecordService.getOverdueRecords();
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Get overdue books successfully", response));
        } catch (Exception ex) {
            log.error("Unexpected error when getting overdue records", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
    @Operation(summary = "Search Borrow Records", description = "Search and filter borrow records")
    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<?>>> searchBorrowRecords(
            @Parameter(description = "Filter by student ID")
            @RequestParam(required = false) Long studentId,

            @Parameter(description = "Filter by book ID")
            @RequestParam(required = false) Long bookId,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) BorrowStatus status,

            @Parameter(description = "From date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "To date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "borrowDate:desc") String sortBy) {

        try {
            log.debug("API search borrow records called");
            PageResponse<?> response = borrowRecordService.searchBorrowRecords(
                    studentId, bookId, status, fromDate, toDate, pageNo, pageSize, sortBy);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Get borrow records successfully", response));
        } catch (Exception ex) {
            log.error("Unexpected error when searching borrow records", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
}
