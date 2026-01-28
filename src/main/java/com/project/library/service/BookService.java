package com.project.library.service;

import com.project.library.dto.request.book.*;
import com.project.library.dto.response.*;
import com.project.library.utils.BookStatus;
import com.project.library.utils.TimeRange;

import java.util.List;

public interface BookService {

    BookResponse create(CreateBookRequest request);
    BookResponse getById(Long id);
    BookResponse update(Long id, UpdateBookRequest request);
    BookResponse updateStatus(Long id, UpdateBookStatusRequest request);

    PageResponse<?> searchBooks(String search, Integer categoryId, BookStatus status, Boolean onlyAvailable, int pageNo, int pageSize, String sortBy);
    List<MostBorrowedBookResponse> getMostBorrowedBooks(int limit, TimeRange timeRange);
    BookResponse adjustQuantity(Long id, AdjustQuantityRequest request);
    List<BookResponse> getLowStockBooks(int threshold);
    BulkImportResultResponse bulkImportBooks(List<BulkImportBookRequest> requests);
    List<BookUsageResponse> getBookUsageAnalysis();

}
