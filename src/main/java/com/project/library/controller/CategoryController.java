package com.project.library.controller;


import com.project.library.dto.request.category.CreateCategoryRequest;
import com.project.library.dto.response.CategoryResponse;
import com.project.library.dto.response.ResponseData;
import com.project.library.exception.BusinessException;
import com.project.library.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@Slf4j
@Tag(name = "Category Controller")
@Validated
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create Category", description = "Api create new category")
    @PostMapping("/")
    public ResponseEntity<ResponseData<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {

        try{
            log.info("API create category called, code={}", request.getCode());
            CategoryResponse response = categoryService.create(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseData<>(HttpStatus.CREATED.value(), "Category created successfully", response));
        }catch(BusinessException ex){
            // Bắt lỗi nghiệp vụ ở đây
            log.warn("Business error when creating category, code={}, message={}",
                    request.getCode(), ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        }catch(Exception ex){
            log.error("Unexpected error when creating category, code = {} ", request.getCode(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Get all categories")
    @GetMapping
    public ResponseEntity<ResponseData<List<CategoryResponse>>> getAllCategories() {
        try {
            log.debug("API get all categories called");
            List<CategoryResponse> response = categoryService.getAllCategories();
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Get categories successfully", response));
        } catch (Exception ex) {
            log.error("Unexpected error when fetching categories", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
}
