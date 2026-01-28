package com.project.library.dto.request.borrow;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenewBorrowRequest {

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String note;

}
