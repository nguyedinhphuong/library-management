package com.project.library.converter;

import com.project.library.dto.response.BookSummaryResponse;
import com.project.library.dto.response.ReviewResponse;
import com.project.library.dto.response.StudentSummaryResponse;
import com.project.library.model.BookReview;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ReviewMapper {
    public ReviewMapper() {}
    public static ReviewResponse toResponse(BookReview review, Long currentStudentId) {
        BookSummaryResponse book = BookMapper.toSummaryResponse(review.getBook());
        StudentSummaryResponse student = StudentSummaryResponse.builder()
                .id(review.getStudent().getId())
                .studentCode(review.getStudent().getStudentCode())
                .fullName(review.getStudent().getFullName())
                .email(review.getStudent().getEmail())
                .phone(review.getStudent().getPhone())
                .build();
        List<String> tags = review.getTags() != null && !review.getTags().isEmpty()
                ? Arrays.asList(review.getTags().split(","))
                : Collections.emptyList();
        boolean canMarkHelpful = currentStudentId != null && !currentStudentId.equals(review.getStudent().getId());
        return ReviewResponse.builder()
                .id(review.getId())
                .book(book)
                .student(student)
                .rating(review.getRating())
                .review(review.getReview())
                .tags(tags)
                .helpfulCount(review.getHelpfulCount())
                .isVerifiedBorrow(review.getIsVerifiedBorrow())
                .canMarkHelpful(canMarkHelpful)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
