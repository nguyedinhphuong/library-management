package com.project.library.controller;


import com.project.library.dto.request.book.*;
import com.project.library.dto.response.*;
import com.project.library.exception.BusinessException;
import com.project.library.service.BookService;
import com.project.library.utils.BookStatus;
import com.project.library.utils.TimeRange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@Slf4j
@Tag(name = "Books Controller")
@Validated
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Create book", description =  " Add new book to library")
    @PostMapping("/")
    public ResponseEntity<ResponseData<BookResponse>> createBook(@Valid @RequestBody CreateBookRequest request){
        try{
            log.info("API create book called, title = {} ", request.getTitle());
            BookResponse response = bookService.create(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseData<>(HttpStatus.CREATED.value(), "Book created successfully ", response));
        }catch (BusinessException ex) {
            log.warn("Business error when creating book, message = {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when creating book", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
    @Operation(summary = "Get Book by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<BookResponse>> getBookById(@PathVariable Long id) {
        try {
            log.debug("API get book by id called, id = {}", id);
            BookResponse response = bookService.getById(id);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Get book successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when getting book, id = {}, message = {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when getting book, id = {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Update Book")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {
        try {
            log.info("API update book called, id = {}", id);
            BookResponse response = bookService.update(id, request);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Book updated successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when updating book, id = {}, message = {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when updating book, id = {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Update Book Status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ResponseData<BookResponse>> updateBookStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookStatusRequest request) {
        try {
            log.info("API update book status called, id = {}, status = {}", id, request.getStatus());
            BookResponse response = bookService.updateStatus(id, request);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Book status updated successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when updating book status, id = {}, message = {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when updating book status, id = {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
    @Operation(summary = "Search Books", description = "Search and filter books with pagination")
    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<?>>> searchBooks(
            @Parameter(description = "Search by title, author, or ISBN")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Integer categoryId,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) BookStatus status,

            @Parameter(description = "Only show available books")
            @RequestParam(required = false) Boolean onlyAvailable,

            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id:desc") String sortBy
    ){
        try {
            log.debug("API search books called");
            PageResponse<?> response = bookService.searchBooks(search, categoryId, status,
                    onlyAvailable, pageNo, pageSize, sortBy);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Get books successfully", response));
        } catch (Exception ex) {
            log.error("Unexpected error when searching books", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
    @Operation(summary = "Get Most Borrowed Books", description = "Get top books by borrow count")
    @GetMapping("/most-borrowed")
    public ResponseEntity<ResponseData<List<MostBorrowedBookResponse>>> getMostBorrowedBooks(
            @Parameter(description = "Limit (max: 50)")
            @RequestParam(defaultValue = "10") int limit,

            @Parameter(description = "Time range")
            @RequestParam(defaultValue = "ALL_TIME") TimeRange timeRange) {

        try {
            log.debug("API get most borrowed books called, limit = {}, timeRange = {}", limit, timeRange);
            List<MostBorrowedBookResponse> response = bookService.getMostBorrowedBooks(limit, timeRange);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Get most borrowed books successfully", response));
        } catch (Exception ex) {
            log.error("Unexpected error when getting most borrowed books", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Adjust Book Quantity", description = "Increase or decrease book quantity")
    @PutMapping("/{id}/adjust-quantity")
    public ResponseEntity<ResponseData<BookResponse>> adjustQuantity(
            @PathVariable Long id,
            @Valid @RequestBody AdjustQuantityRequest request) {
        try {
            log.info("API adjust quantity called, bookId: {}, adjustment: {}", id, request.getAdjustment());
            BookResponse response = bookService.adjustQuantity(id, request);
            String message = request.getAdjustment() > 0
                    ? String.format("Added %d book(s) successfully", request.getAdjustment())
                    : String.format("Removed %d book(s) successfully", Math.abs(request.getAdjustment()));
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), message, response));
        } catch (BusinessException ex) {
            log.warn("Business error when adjusting quantity, id: {}, message: {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when adjusting quantity, id: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Get Low Stock Books", description = "Get books with low available quantity")
    @GetMapping("/low-stock")
    public ResponseEntity<ResponseData<List<BookResponse>>> getLowStockBooks(
            @Parameter(description = "Stock threshold (0-10)")
            @RequestParam(defaultValue = "2") int threshold) {
        try {
            log.debug("API get low stock books called, threshold: {}", threshold);
            List<BookResponse> response = bookService.getLowStockBooks(threshold);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(),
                            String.format("Found %d book(s) with low stock", response.size()),
                            response));
        } catch (BusinessException ex) {
            log.warn("Business error when getting low stock books, message: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when getting low stock books", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Bulk Import Books", description = "Import multiple books at once")
    @PostMapping("/bulk-import")
    public ResponseEntity<ResponseData<BulkImportResultResponse>> bulkImportBooks (@Valid @RequestBody List<BulkImportBookRequest> requests) {
        try{
            log.info("API bulk import books called, total: {}" , requests.size());

            if(requests.isEmpty()) return ResponseEntity.badRequest().body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Request list is empty"));
            if(requests.size() > 1000) return ResponseEntity.badRequest().body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Cannot import more than 1000 books at once"));
            BulkImportResultResponse response = bookService.bulkImportBooks(requests);
            String message = String.format("Import completed: %d success, %d failed, %d skipped", response.getSuccessCount(), response.getFailedCount(), response.getSkippedCount());
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), message, response));
        }catch(Exception e){
            log.error("Unexpected error when bulk importing books ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Interval server error "));
        }

    }
}
