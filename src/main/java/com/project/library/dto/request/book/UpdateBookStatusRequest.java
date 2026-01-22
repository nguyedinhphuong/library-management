package com.project.library.dto.request.book;

import com.project.library.utils.BookStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UpdateBookStatusRequest implements Serializable {

    @NotNull(message = "Status must not be null")
    private BookStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
