package com.project.library.converter;


import com.project.library.dto.request.category.CreateCategoryRequest;
import com.project.library.dto.response.CategoryResponse;
import com.project.library.model.Category;

public final class CategoryMapper {

    private CategoryMapper() {}

    public static Category toEntity(CreateCategoryRequest request){
        return Category.builder()
                .code(request.getCode())
                .name(request.getName())
                .build();
    }

    public static CategoryResponse toResponse(Category category){
        return CategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .build();
    }
}
