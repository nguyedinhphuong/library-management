package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResponse implements Serializable {
    private Long id;
    private BookSummaryResponse book;
    private StudentSummaryResponse student;
    private Integer rating;
    private String review;
    private List<String> tags;
    private Integer helpfulCount;
    private Boolean isVerifiedBorrow;
    private Boolean canMarkHelpful;
    private LocalDateTime createdAt;
}
