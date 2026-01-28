package com.project.library.dto.request.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AdjustQuantityRequest implements Serializable {
    @NotNull(message = "Adjustment must not be null")
    @Min(value = -100, message = "Cannot decrease more than 100 at once")
    @Max(value = 100, message = "Cannot increase more than 100 at once")
    private Integer adjustment;

    @NotNull(message = "Reason must not be null")
    @Size(min =  10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
}
