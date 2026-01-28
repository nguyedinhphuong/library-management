package com.project.library.service;

import com.project.library.dto.request.student.CreateStudentRequest;
import com.project.library.dto.request.student.IncreaseLimitRequest;
import com.project.library.dto.request.student.UpdateStudentRequest;
import com.project.library.dto.request.student.UpdateStudentStatusRequest;
import com.project.library.dto.response.BorrowRecordResponse;
import com.project.library.dto.response.PageResponse;
import com.project.library.dto.response.StudentResponse;
import com.project.library.utils.BorrowStatus;

import java.util.List;

public interface StudentService {

    StudentResponse create(CreateStudentRequest request);
    StudentResponse getById(Long id);
    StudentResponse update(Long id, UpdateStudentRequest request);
    StudentResponse updateStatus(Long id, UpdateStudentStatusRequest request);

    // search
    PageResponse<?> advanceStudents(int pageNo, int pageSize, String sortBy, String... search);
    PageResponse<?> getStudentBorrowHistory(Long studentId, BorrowStatus status, int pageNo, int pageSize);

    List<BorrowRecordResponse> getCurrentBorrow(Long studentId);

    StudentResponse increaseLimit(Long id, IncreaseLimitRequest request);
}
