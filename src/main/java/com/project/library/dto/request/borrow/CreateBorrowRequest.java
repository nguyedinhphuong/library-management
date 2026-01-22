package com.project.library.dto.request.borrow;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CreateBorrowRequest implements Serializable {

    @NotNull(message = "Student ID must not be null")
    private Long studentId;

    @NotNull(message = "Student ID must not be null")
    private Long bookId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
