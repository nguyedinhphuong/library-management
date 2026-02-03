package com.project.library.repository;

import com.project.library.model.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {

    Page<BookReview> findByBookIdOrderByCreatedAtDesc(Long id, Pageable pageable);
    Optional<BookReview> findByBookIdAndStudentId(Long bookId, Long studentId);
    boolean existsByBookIdAndStudentId(Long bookId, Long studentId);

}
