package com.project.library.dto.request.book;


import com.project.library.utils.ISBN;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BulkImportBookRequest implements Serializable {


    @NotBlank(message = "Title must not be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Author must not be blank")
    @Size(max = 100, message = "Author must not exceed 100 characters")
    private String author;

    @NotBlank(message = "ISBN must not be blank")
    @ISBN
    private String isbn;

    @NotNull(message = "Category ID must not be null")
    private Integer categoryId;

    @NotNull(message = "Quantity total must not be null")
    @Min(value = 1, message = "Quantity total must be at least 1")
    private Integer quantityTotal;


}
