package com.project.library.controller;

import com.project.library.dto.request.review.CreateReviewRequest;
import com.project.library.dto.response.ResponseData;
import com.project.library.dto.response.ReviewResponse;
import com.project.library.exception.BusinessException;
import com.project.library.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@Slf4j
@Tag(name = "Review Controller")
@RequiredArgsConstructor
public class BookReviewController {

    private ReviewService reviewService;

    @Operation(summary = "Create Review", description = "Student creates a book review")
    @PostMapping
    public ResponseEntity<ResponseData<ReviewResponse>> createReview(
            @Parameter(description = "Student ID (from auth token in real app)")
            @RequestHeader(value = "X-Student-Id",required = true) Long studentId,
            @Valid @RequestBody CreateReviewRequest request) {

        try {
            log.info("API create review called - studentId: {}, bookId: {}", studentId, request.getBookId());
            ReviewResponse response = reviewService.createReview(studentId, request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseData<>(HttpStatus.CREATED.value(),
                            "Review created successfully. Thank you for sharing!", response));
        } catch (BusinessException ex) {
            log.warn("Business error when creating review, message: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when creating review", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

}
