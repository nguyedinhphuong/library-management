package com.project.library.dto.request.borrow;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ReturnBookRequest implements Serializable {

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

}
