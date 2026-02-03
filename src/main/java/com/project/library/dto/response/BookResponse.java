package com.project.library.dto.response;

import com.project.library.utils.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private CategorySummaryResponse category;
    private Integer quantityTotal;
    private Integer quantityAvailable;
    private BookStatus status;
    private boolean isAvailable;

    private String coverImageUrl;
    private Boolean hasCoverImage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
