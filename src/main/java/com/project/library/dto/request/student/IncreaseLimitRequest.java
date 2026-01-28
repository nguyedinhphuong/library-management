package com.project.library.dto.request.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class IncreaseLimitRequest implements Serializable {
    @NotNull(message = "New limit must not be null")
    @Min(value = 1, message = "New limit must be at least 1")
    @Max(value = 15, message = "New limit cannot exceed 15")
    private Integer newLimit;

    @NotNull(message = "Reason must not be null")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
}
