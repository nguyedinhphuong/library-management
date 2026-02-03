package com.project.library.service;

import com.project.library.dto.request.review.CreateReviewRequest;
import com.project.library.dto.response.ReviewResponse;

public interface ReviewService {

    ReviewResponse createReview(Long studentId, CreateReviewRequest request);
}
