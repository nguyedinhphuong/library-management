package com.project.library.converter;

import com.project.library.dto.request.book.CreateBookRequest;
import com.project.library.dto.request.book.UpdateBookRequest;
import com.project.library.dto.response.BookResponse;
import com.project.library.dto.response.BookSummaryResponse;
import com.project.library.dto.response.CategorySummaryResponse;
import com.project.library.model.Book;
import com.project.library.model.Category;
import com.project.library.utils.BookStatus;

public class BookMapper {

    public BookMapper() {}

    public static Book toEntity(CreateBookRequest request, Category category){
        return Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .category(category)
                .quantityTotal(request.getQuantityTotal())
                .quantityAvailable(request.getQuantityTotal())
                .status(BookStatus.AVAILABLE)
                .coverImageUrl(null)
                .coverImagePublicId(null)
                .build();
    }

    public static BookResponse toResponse(Book book){
        CategorySummaryResponse categorySummaryResponse = CategorySummaryResponse.builder()
                .id(book.getCategory().getId())
                .code(book.getCategory().getCode())
                .name(book.getCategory().getName())
                .build();

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .category(categorySummaryResponse)
                .quantityTotal(book.getQuantityTotal())
                .quantityAvailable(book.getQuantityAvailable())
                .status(book.getStatus())
                .isAvailable(book.getQuantityAvailable() > 0 && book.getStatus() == BookStatus.AVAILABLE)
                .coverImageUrl(book.getCoverImageUrl())
                .hasCoverImage(book.hasCoverImage())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    public static BookSummaryResponse toSummaryResponse(Book book) {
        return BookSummaryResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .build();
    }

    public static void updateEntity(Book book, UpdateBookRequest request, Category category){
        if(request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }
        if (category != null) {
            book.setCategory(category);
        }

        if(request.getQuantityTotal() != null) {
            int currentBorrowing = book.getQuantityTotal() - book.getQuantityAvailable();
            book.setQuantityTotal(request.getQuantityTotal());
            book.setQuantityAvailable(request.getQuantityTotal() - currentBorrowing);
        }
    }
}
