package com.project.library.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BulkImportResultResponse implements Serializable {
    private Integer totalRecords;
    private Integer successCount;
    private Integer failedCount;
    private Integer skippedCount;
    private List<String> errors;
    private List<BookResponse> importedBooks;
}
