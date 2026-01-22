package com.project.library.service.impl;

import com.project.library.converter.BookMapper;
import com.project.library.dto.request.book.CreateBookRequest;
import com.project.library.dto.request.book.UpdateBookRequest;
import com.project.library.dto.request.book.UpdateBookStatusRequest;
import com.project.library.dto.response.BookResponse;
import com.project.library.dto.response.BookSummaryResponse;
import com.project.library.dto.response.MostBorrowedBookResponse;
import com.project.library.dto.response.PageResponse;
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
import java.util.ArrayList;
import java.util.List;

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
