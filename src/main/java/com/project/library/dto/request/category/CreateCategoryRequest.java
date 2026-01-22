package com.project.library.dto.request.category;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CreateCategoryRequest implements Serializable {

    @NotBlank(message = "Category code must not be blank")
    @Size(max = 20, message = "Category code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Category name must not be blank")
    @Size(min = 3, max = 100, message = "Category name must be between 3 to 100 characters")
    private String name;
}
