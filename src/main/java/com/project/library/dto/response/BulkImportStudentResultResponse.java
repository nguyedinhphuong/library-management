package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BulkImportStudentResultResponse {
    private Integer totalRecords;
    private Integer successCount;
    private Integer failedCount;
    private Integer skippedCount;
    private List<String> errors;
    private List<StudentResponse> importedStudents;
}
