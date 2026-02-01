package com.project.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_book_review", uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "student_id"}))
public class BookReview extends AbstractEntity<Long> implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1 den 5 sao

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "tag")
    private String tags; // [programming, essential,... ]

    @Column(name = "helpful_count", nullable = false)
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(name = "is_verified_borrow", nullable = false)
    @Builder.Default
    private Boolean isVerifiedBorrow = false;// only who borrowed can review
}
