package com.project.library.service.impl;

import com.project.library.converter.BookMapper;
import com.project.library.converter.CategoryMapper;
import com.project.library.dto.request.category.CreateCategoryRequest;
import com.project.library.dto.response.BookResponse;
import com.project.library.dto.response.CategoryResponse;
import com.project.library.exception.BusinessException;
import com.project.library.model.Book;
import com.project.library.model.Category;
import com.project.library.repository.BookRepository;
import com.project.library.repository.CategoryRepository;
import com.project.library.service.BookService;
import com.project.library.service.CategoryService;
import com.project.library.utils.BookStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {

        log.info("Create category request received, code = {} ", request.getCode());
        categoryRepository.findByCode(request.getCode())
                .ifPresent(c-> {throw new BusinessException("Category code already exists");
                });
        try {
            Category saved = categoryRepository.save(CategoryMapper.toEntity(request));
            log.info("Category created successfully, id = {}, code = {} ", saved.getId(), saved.getCode());
            return CategoryMapper.toResponse(saved);
        }catch (DataIntegrityViolationException e) {
            log.error("Race condition detected when creating category, code={}",request.getCode() , e );
            throw new BusinessException("Category code already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");
        List<CategoryResponse> result = categoryRepository.findAll()
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();
        log.debug("Fetched {} categories", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByCategory(Integer categoryId, Boolean onlyAvailable) {

        log.debug("Get books by category - categoryId: {}, onlyAvailable: {}", categoryId, onlyAvailable);
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new BusinessException("Category not found with id: " + categoryId));

        List<Book> book = category.getBooks();
        if(onlyAvailable != null && onlyAvailable) {
            book = book.stream()
                    .filter(b -> b.getQuantityAvailable() > 0 && b.getStatus() == BookStatus.AVAILABLE)
                    .toList();
        }
        List<BookResponse> responses = book.stream()
                .map(BookMapper::toResponse)
                .sorted((a,b) -> a.getTitle().compareTo(b.getTitle()))
                .toList();
        log.debug("Found {} books in category {}", responses.size(), category.getName());
        return responses;
    }
}
