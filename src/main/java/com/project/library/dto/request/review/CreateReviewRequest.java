package com.project.library.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CreateReviewRequest implements Serializable {

    @NotNull(message = "Book Id must not be null")
    private Long bookId;

    @NotNull(message = "Rating must not be null")
    @Min(value = 1, message = "Rating must be between 1 and 5 ")
    @Max(value = 5, message = "Rating must be between 1 and 5 ")
    private Integer rating;

    @Size(min = 10, max = 2000, message = "Review must be between 10 and 2000 characters")
    private String review;

    @Size(max = 5, message = "Maximum 5 tags allowed")
    private List<String> tags;
}
