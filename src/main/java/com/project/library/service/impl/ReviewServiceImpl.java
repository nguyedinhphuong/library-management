package com.project.library.service.impl;

import com.project.library.converter.ReviewMapper;
import com.project.library.dto.request.review.CreateReviewRequest;
import com.project.library.dto.response.ReviewResponse;
import com.project.library.exception.BusinessException;
import com.project.library.model.Book;
import com.project.library.model.BookReview;
import com.project.library.model.Student;
import com.project.library.repository.BookRepository;
import com.project.library.repository.BookReviewRepository;
import com.project.library.repository.BorrowRecordRepository;
import com.project.library.repository.StudentRepository;
import com.project.library.service.AchievementService;
import com.project.library.service.ReviewService;
import com.project.library.utils.BorrowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final BookReviewRepository bookReviewRepository;
    private final StudentRepository studentRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final AchievementService achievementService;

    @Override
    @Transactional
    @CacheEvict(value = "bookReviews" , key = "#studentId")
    public ReviewResponse createReview(Long studentId, CreateReviewRequest request) {
        log.info("Create review - studentId: {}, bookId: {}, rating: {}",
                studentId, request.getBookId(), request.getRating());
        // check student ton tai
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Student not found "));
        // check book
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BusinessException("Book not found"));
        // cannot review twice
        if(bookReviewRepository.existsByBookIdAndStudentId(request.getBookId(),studentId)){
            throw new BusinessException("You have already reviewed this book");
        }
        // must have borrowed this book
        boolean hasBorrowed = borrowRecordRepository.existsByStudentIdAndBookIdAndStatus(studentId, request.getBookId(), BorrowStatus.RETURNED);
        if(!hasBorrowed) {
            log.warn("Student {} review book {} without borrowing history", student.getFullName(), book.getTitle());
        }
        BookReview review = BookReview.builder()
                .book(book)
                .student(student)
                .rating(request.getRating())
                .review(request.getReview())
                .tags(request.getTags() != null ? String.join(", ", request.getTags()) : null)
                .helpfulCount(0)
                .isVerifiedBorrow(hasBorrowed)
                .build();
        BookReview saved = bookReviewRepository.save(review);
        achievementService.checkAndUnlockAchievements(studentId);
        log.info("Review created successfully - reviewId: {}, rating: {}", saved.getId(), saved.getRating());
        return ReviewMapper.toResponse(saved, studentId);
    }
}
