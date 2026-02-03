package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BookReviewSummaryResponse implements Serializable {
    private Long bookId;
    private String title;
    private Double averageRating;
    private Integer totalReviews;
    private RatingDistribution distribution;
    private List<ReviewResponse> recentReviews;
}
