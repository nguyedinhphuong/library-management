package com.project.library.dto.request.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UpdateBookRequest implements Serializable {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 100, message = "Author must not exceed 100 characters")
    private String author;

    private Integer categoryId;

    @Min(value = 1, message = "Quantity total must be at least 1")
    private Integer quantityTotal;


}