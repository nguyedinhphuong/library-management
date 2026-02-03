package com.project.library.service;


import com.project.library.dto.request.category.CreateCategoryRequest;
import com.project.library.dto.response.BookResponse;
import com.project.library.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);
    List<CategoryResponse> getAllCategories();
    List<BookResponse> getBooksByCategory(Integer categoryId, Boolean onlyAvailable);

}
