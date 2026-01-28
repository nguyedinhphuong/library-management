package com.project.library.service.impl;


import com.project.library.converter.BorrowRecordMapper;
import com.project.library.dto.request.borrow.CreateBorrowRequest;
import com.project.library.dto.request.borrow.RenewBorrowRequest;
import com.project.library.dto.request.borrow.ReportLostRequest;
import com.project.library.dto.request.borrow.ReturnBookRequest;
import com.project.library.dto.response.BorrowRecordResponse;
import com.project.library.dto.response.PageResponse;
import com.project.library.exception.BusinessException;
import com.project.library.model.Book;
import com.project.library.model.BorrowRecord;
import com.project.library.model.Student;
import com.project.library.repository.BookRepository;
import com.project.library.repository.BorrowRecordRepository;
import com.project.library.repository.StudentRepository;
import com.project.library.repository.criteria.BorrowRecordSearchRepository;
import com.project.library.service.BorrowRecordService;
import com.project.library.utils.BookStatus;
import com.project.library.utils.BorrowStatus;
import com.project.library.utils.StudentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class BorrowRecordServiceImpl implements BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final StudentRepository studentRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordSearchRepository borrowRecordSearchRepository;

    @Override
    @Transactional
    public BorrowRecordResponse borrowBook(CreateBorrowRequest request) {
        log.info("Borrow book request - studentId: {}, bookId: {}",
                request.getStudentId(), request.getBookId());
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("Student not found with id: " + request.getStudentId()));
        if(student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException("Student is suspended, cannot borrow books");
        }
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BusinessException("Book not found with id: " + request.getBookId()));

        if(book.getQuantityAvailable() <= 0) {
            throw new BusinessException("Book is out of stock. Please wait for return.");
        }
        if (book.getStatus() != BookStatus.AVAILABLE) {
            String statusMessage = switch (book.getStatus()) {
                case MAINTENANCE -> "Book is under maintenance";
                case LOST -> "Book is lost";
                case DAMAGED -> "Book is damaged";
                case ARCHIVED -> "Book is archived";
                default -> "Book is not available";
            };
            throw new BusinessException(statusMessage);
        }
        // Student hasn't reached borrowing limit?
        long currentBorrowingCount = borrowRecordRepository.countByStudentIdAndStatus(student.getId(), BorrowStatus.BORROWING);
        if(currentBorrowingCount >= student.getMaxBorrowLimit()) {
            throw new BusinessException(
                    String.format("You have reached the borrowing limit (%d/%d). Please return books before borrowing more.",
                            currentBorrowingCount, student.getMaxBorrowLimit())
            );
        }
        //Student is not already borrowing this book?
        boolean alreadyBorrowing = borrowRecordRepository.existsByStudentIdAndBookIdAndStatus(student.getId(), book.getId(), BorrowStatus.BORROWING);
        if(alreadyBorrowing) throw new BusinessException("You are already borrowing this book. Please return it before borrowing again.");

        BorrowRecord borrowRecord = BorrowRecordMapper.toEntity(request, student, book);
        book.setQuantityAvailable(book.getQuantityAvailable() - 1);
        BorrowRecord saved = borrowRecordRepository.save(borrowRecord);
        bookRepository.save(book);
        log.info("Book borrowed successfully - recordId: {}, studentCode: {}, bookTitle: {}",
                saved.getId(), student.getStudentCode(), book.getTitle());
        return BorrowRecordMapper.toResponse(saved);
    }

    @Override
    public BorrowRecordResponse getById(Long id) {
        log.debug("Get borrow record by id: {}", id);
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Borrow record not found with id: " + id));
        return BorrowRecordMapper.toResponse(record);
    }

    @Override
    @Transactional
    public BorrowRecordResponse returnBook(Long id, ReturnBookRequest request) {
        log.info("Return book request - borrowRecordId: {} ", id);
        // book record exists ?
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Borrow record not found with id: " + id));
        // book has't been returned yet ?
        if(record.getReturnDate() != null) throw new BusinessException("This book has already been returned on " + record.getReturnDate());
        // update borrow record
        record.setReturnDate(LocalDate.now());
        record.setStatus(BorrowStatus.RETURNED);
        //Merge notes if provided
        if(StringUtils.hasLength(request.getNotes())) {
            String existingNotes = record.getNotes();
            String newNotes = StringUtils.hasText(existingNotes)
                    ? existingNotes + " | "+ request.getNotes()
                    : record.getNotes();
            record.setNotes(newNotes);
        }
        // increase book quantity
        Book book = record.getBook();
        book.setQuantityAvailable(book.getQuantityAvailable() + 1);


        BorrowRecord updated = borrowRecordRepository.save(record);
        bookRepository.save(book);
        log.info("Book returned successfully - recordId: {}, bookTitle: {}, returnDate: {}",
                updated.getId(), book.getTitle(), updated.getReturnDate());
        return BorrowRecordMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getOverdueRecords() {
        log.debug("Get overdue borrow records");

        LocalDate today = LocalDate.now();
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(today);
        List<BorrowRecordResponse> responses = overdueRecords.stream()
                .map(BorrowRecordMapper::toResponse)
                .toList();
        log.debug("Found {} overdue records", responses.size());
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<?> searchBorrowRecords(Long studentId, Long bookId, BorrowStatus status, LocalDate fromDate, LocalDate toDate, int pageNo, int pageSize, String sortBy) {
        log.debug("Search borrow records - studentId: {}, bookId: {}, status: {}",
                studentId, bookId, status);

        if (pageSize > 100) {
            pageSize = 100;
            log.warn("Page size exceeds maximum, set to 100");
        }

        PageResponse<?> pageResponse = borrowRecordSearchRepository.searchBorrowRecords(
                studentId, bookId, status, fromDate, toDate, pageNo, pageSize, sortBy);

        @SuppressWarnings("unchecked")
        List<BorrowRecord> records =(List<BorrowRecord>) pageResponse.getItem();
        List<BorrowRecordResponse> responses = records.stream()
                .map(BorrowRecordMapper::toResponse)
                .toList();
        return PageResponse.builder()
                .pageNo(pageResponse.getPageNo())
                .pageSize(pageResponse.getPageSize())
                .totalPages(pageResponse.getTotalPages())
                .totalElements(pageResponse.getTotalElements())
                .item(responses)
                .build();
    }

    @Override
    public BorrowRecordResponse renewBorrow(Long id, RenewBorrowRequest request) {
        log.info("Renew borrow request - borrowRecordId: {}", id);

        // check có borrowRecord chưa
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Borrow record not found with id: " + id));

        // must be currently borrowing
        if(record.getStatus() != BorrowStatus.BORROWING){
            throw new BusinessException("Can only renew books that are currently borrowed");
        }
        // not overdue
        if(LocalDate.now().isAfter(record.getDueDate())){
            throw new BusinessException("Cannot renew overdue books. Please return first.");
        }
        // not exceeded renewal limit
        if(record.getRenewCount() >= record.getMaxRenewals()) {
            throw new BusinessException(
                    String.format("Maximum renewal limit reached (%d/%d). Cannot renew anymore.",
                            record.getRenewCount(), record.getMaxRenewals())
            );
        }

        record.setDueDate(record.getDueDate().plusDays(7)); // cho phép thêm 7 ngày nữa
        record.setRenewCount(record.getRenewCount() + 1);

        if(StringUtils.hasText(record.getNotes())) {
            String existingNotes = record.getNotes();
            String newNotes = StringUtils.hasText(existingNotes)
                    ? existingNotes + " | Renewal: "+ request.getNote()
                    : "Renewal: " + request.getNote();
            record .setNotes(newNotes);
        }
        BorrowRecord updated = borrowRecordRepository.save(record);
        log.info("Borrow renewed successfully - recordId: {}, newDueDate: {}, renewalCount: {}/{}",
                updated.getId(), updated.getDueDate(), updated.getRenewCount(), updated.getMaxRenewals());
        return BorrowRecordMapper.toResponse(updated) ;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getDueSoonRecords(int days) {
        log.debug("Get due soon records - within {} days", days);

        if(days < 1 || days > 30) {
            throw  new BusinessException("Days must be between 1 t0 30");
        }
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(days);
        List<BorrowRecord> records = borrowRecordRepository.findDueSoonRecords(today, targetDate);
        List<BorrowRecordResponse> responses = records.stream()
                .map(BorrowRecordMapper::toResponse)
                .toList();
        log.debug("Found {} borrows due within {} days", responses.size(), days);
        return responses;
    }

    @Override
    public BorrowRecordResponse reportLost(Long id, ReportLostRequest request) {
        log.info("Report lost book - borrowRecordId: {}", id);

        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Borrow record not found with id: " + id));
        if(record.getStatus() != BorrowStatus.BORROWING) {
            throw new BusinessException("Can only report lost for books currently borrowed");
        }
        record.setStatus(BorrowStatus.RETURNED);
        record.setReturnDate(LocalDate.now());
        String lostNote = "LOST: "+ request.getReason();
        record.setNotes(StringUtils.hasText(record.getNotes()) ? record.getNotes() + " | " + lostNote : lostNote);
        // cap nhat sach
        Book book = record.getBook();
        book.setQuantityTotal(book.getQuantityTotal() - 1);
        if(book.getQuantityTotal() <= 0) {
            book.setStatus(BookStatus.LOST);
        }
        borrowRecordRepository.save(record);
        bookRepository.save(book);
        log.warn("Book lost: recordId: {}, book: {}, reason: {}", record.getId(), book.getTitle(), request.getReason());
        return BorrowRecordMapper.toResponse(record);
    }
}
