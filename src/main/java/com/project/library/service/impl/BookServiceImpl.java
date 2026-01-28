package com.project.library.service.impl;

import com.project.library.converter.BookMapper;
import com.project.library.dto.request.book.*;
import com.project.library.dto.response.*;
import com.project.library.exception.BusinessException;
import com.project.library.model.Book;
import com.project.library.model.Category;
import com.project.library.repository.BookRepository;
import com.project.library.repository.CategoryRepository;
import com.project.library.repository.criteria.BookSearchRepository;
import com.project.library.service.BookService;
import com.project.library.utils.BookStatus;
import com.project.library.utils.TimeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookSearchRepository bookSearchRepository;

    @Override
    @Transactional
    public BookResponse create(CreateBookRequest request) {
        log.info("Create book request received, title = {}, isbn = {}", request.getTitle(), request.getIsbn());

        // check category
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new BusinessException("Category not found with id: " + request.getCategoryId()));
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException("ISBN already exists: " + request.getIsbn());
        }
        try {
            Book book = BookMapper.toEntity(request, category);
            Book saved = bookRepository.save(book);
            log.info("Book created successfully, id = {}, isbn = {}", saved.getId(), saved.getIsbn());
            return BookMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("Race condition detected when creating book, isbn = {}", request.getIsbn(), e);
            throw new BusinessException("ISBN already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        log.debug("Fetching book by id = {}", id);
        Book book = getBookById(id);
        return BookMapper.toResponse(book);
    }

    @Override
    public BookResponse update(Long id, UpdateBookRequest request) {
        log.info("Update book request received, id = {}", id);
        Book book = getBookById(id);
        Category category = null;

        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException("Category not found with id: " + request.getCategoryId()));
        }
        // validate quantityTotal is being updated
        if (request.getQuantityTotal() != null) {
            int currentBorrowing = book.getQuantityTotal() - book.getQuantityAvailable();
            if (request.getQuantityTotal() < currentBorrowing) {
                throw new BusinessException(
                        "Cannot reduce quantity to " + request.getQuantityTotal() +
                                " because " + currentBorrowing + " books are currently borrowed"
                );
            }
        }
        BookMapper.updateEntity(book, request, category);
        Book updated = bookRepository.save(book);

        log.info("Book updated successfully, id = {} ", updated.getId());
        return BookMapper.toResponse(book);
    }

    @Override
    public BookResponse updateStatus(Long id, UpdateBookStatusRequest request) {
        log.info("Update book status request received, id = {}, status = {}", id, request.getStatus());

        Book book = getBookById(id);
        book.setStatus(request.getStatus());
        Book updated = bookRepository.save(book);
        log.info("Book status updated successfully, id = {}, status = {}, reason = {}",
                id, request.getStatus(), request.getReason());
        return BookMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public PageResponse<?> searchBooks(String search, Integer categoryId, BookStatus status, Boolean onlyAvailable, int pageNo, int pageSize, String sortBy) {
        log.debug("Search books - search: {}, categoryId: {}, status: {}, onlyAvailable: {}",
                search, categoryId, status, onlyAvailable);

        if (pageSize > 100) {
            pageSize = 100;
            log.warn("Page size exceeds maximum, set to 100");
        }
        PageResponse<?> pageResponse = bookSearchRepository.searchBooks(search, categoryId, status, onlyAvailable, pageNo, pageSize, sortBy);

        List<Book> books = (List<Book>) pageResponse.getItem();
        List<BookResponse> bookResponses = books.stream()
                .map(BookMapper::toResponse)
                .toList();
        return PageResponse.builder()
                .item(bookResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MostBorrowedBookResponse> getMostBorrowedBooks(int limit, TimeRange timeRange) {

        log.debug("Get most borrowed books - limit: {}, timeRange: {}", limit, timeRange);

        if (limit > 50) {
            limit = 50;
            log.warn("Limit exceeds maximum, set to 50");
        }

        LocalDate startDate = calculateStartDate(timeRange);
        List<Object[]> results = bookRepository.findMostBorrowedBooks(startDate, limit);

        List<MostBorrowedBookResponse> response = new ArrayList<>();
        int rank = 1;

        for (Object[] result : results) {
            Book book = (Book) result[0];
            Long totalBorrowCount = ((Number) result[1]).longValue();
            Long currentBorrowing = ((Number) result[2]).longValue();

            BookSummaryResponse bookSummary = BookMapper.toSummaryResponse(book);
            response.add(MostBorrowedBookResponse.builder()
                    .rank(rank++)
                    .book(bookSummary)
                    .totalBorrowCount(totalBorrowCount)
                    .currentBorrowingCount(currentBorrowing)
                    .build());
        }
        log.debug("Found {} most borrowed books", response.size());
        return response;
    }

    @Override
    @Transactional(readOnly = false)
    public BookResponse adjustQuantity(Long id, AdjustQuantityRequest request) {
        log.info("Adjust quantity request - bookId: {}, adjustment: {}", id, request.getAdjustment());
        // check id
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Book not found with id: " + id));

        int newQuantityTotal = book.getQuantityTotal() + request.getAdjustment(); // chi dieu chinh khong dong vào borrow truc tiep
        int currentBorrowing = book.getQuantityTotal() - book.getQuantityAvailable(); // so sach dang muon khong thay doi khi ajustment

        if(newQuantityTotal < currentBorrowing) {
            throw new BusinessException(
                    String.format("Cannot reduce quantity to %d because %d books are currently borrowed",
                            newQuantityTotal, currentBorrowing)
            );
        }
        if (newQuantityTotal < 0) {
            throw new BusinessException("Quantity cannot be negative");
        }
        book.setQuantityTotal(newQuantityTotal);
        book.setQuantityAvailable(newQuantityTotal - currentBorrowing);
        Book updated = bookRepository.save(book);
        bookRepository.flush();
        log.info("Quantity adjusted - bookId: {}, old: {}, new: {}, reason: {}",
                id, book.getQuantityTotal() - request.getAdjustment(),
                updated.getQuantityTotal(), request.getReason());

        return BookMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getLowStockBooks(int threshold) {
        log.debug("Get low stock books - threshold: {}", threshold);

        if(threshold < 0 || threshold > 10) {
            throw new BusinessException("Threshold must be between 0 to 10");
        }
        List<Book> lowStockBooks = bookRepository.findLowStockBooks(threshold);
        List<BookResponse> responses = lowStockBooks.stream()
                .map(BookMapper::toResponse)
                .toList();
        log.debug("Found {} low stock books", responses.size());
        return responses;
    }

    @Override
    public BulkImportResultResponse bulkImportBooks(List<BulkImportBookRequest> requests) {
        log.info("Bulk import book - total records: {}", requests.size());

        int total = requests.size();
        int success = 0;
        int failed = 0;
        int skipped = 0;

        List<String> errors = new ArrayList<>();
        List<BookResponse> importedBooks = new ArrayList<>();

        List<String> allIsbns = requests.stream()
                .map(BulkImportBookRequest::getIsbn)
                .toList();
        List<Integer> categoryIds = requests.stream()
                .map(BulkImportBookRequest::getCategoryId)
                .distinct()
                .toList();

        // query db duy nhất 1 lần
        Set<String> existingIsbns = new HashSet<>(bookRepository.findExistingIsbns(allIsbns));

        Map<Integer, Category> categoryMap = categoryRepository.findByIdIn(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c->c));

        Set<String> fileIsbns = new HashSet<>();
        for(int i = 0; i < total; i++) {
            BulkImportBookRequest req = requests.get(i);

            try{
                if(!fileIsbns.add(req.getIsbn())) {
                    skipped++;
                    errors.add("Row "+ (i + 1) + ": ISBN duplicated in file - SKIPPED");
                    continue;
                }
                // check trung isbn trong db
                if(existingIsbns.contains(req.getIsbn())) {
                    skipped++;
                    errors.add("Row "+ (i + 1) + ": ISBN already exists - SKIPPED");
                    continue;
                }

                // Check category tồn tại
                Category category = categoryMap.get(req.getCategoryId());
                if(category == null) throw new BusinessException("Category not found: " + req.getCategoryId());
                Book book = Book.builder()
                        .title(req.getTitle())
                        .author(req.getAuthor())
                        .isbn(req.getIsbn())
                        .category(category)
                        .quantityTotal(req.getQuantityTotal())
                        .quantityAvailable(req.getQuantityTotal())
                        .status(BookStatus.AVAILABLE)
                        .build();

                Book saved = bookRepository.save(book);
                importedBooks.add(BookMapper.toResponse(saved));
                success++;
            } catch (Exception e) {
                failed++;
                errors.add("Row " + (i + 1) + ": ISBN " + req.getIsbn()
                        + " - ERROR: " + e.getMessage());
            }
        }
        log.info("Bulk import completed - Success: {}, Failed: {}, Skipped: {}", success, failed, skipped);
        return BulkImportResultResponse.builder()
                .totalRecords(total)
                .successCount(success)
                .failedCount(failed)
                .skippedCount(skipped)
                .errors(errors)
                .importedBooks(importedBooks)
                .build();
    }

    private LocalDate calculateStartDate(TimeRange timeRange) {
        LocalDate now = LocalDate.now();
        return switch (timeRange) {
            case THIS_MONTH -> now.withDayOfMonth(1);
            case THIS_YEAR -> now.withDayOfYear(1);
            case ALL_TIME -> null;
        };
    }
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BusinessException("Book not found with id: " + id));
    }

}
