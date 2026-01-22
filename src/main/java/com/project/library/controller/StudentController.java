package com.project.library.controller;

import com.project.library.dto.request.student.CreateStudentRequest;
import com.project.library.dto.request.student.UpdateStudentRequest;
import com.project.library.dto.request.student.UpdateStudentStatusRequest;
import com.project.library.dto.response.ResponseData;
import com.project.library.dto.response.StudentResponse;
import com.project.library.exception.BusinessException;
import com.project.library.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@Slf4j
@Tag(name = "Student Controller")
@Validated
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Create Student", description = "API create new student with auto-generated student code ")
    @PostMapping
    public ResponseEntity<ResponseData<StudentResponse>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        try {
            log.info("API create student called, fullName = {}", request.getFullName());
            StudentResponse response = studentService.create(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseData<>(HttpStatus.CREATED.value(), "Student created successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when creating student, message = {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when creating student", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Get Student by ID", description = "Get student details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<StudentResponse>> getStudentById(@PathVariable Long id) {
        try {
            log.debug("API get student by id called, id = {}", id);
            StudentResponse response = studentService.getById(id);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Get student successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when getting student, id = {}, message = {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when getting student, id = {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Update Student", description = "Update student information (partial update)")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<StudentResponse>> updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        try {
            log.info("API update student called, id = {}", id);
            StudentResponse response = studentService.update(id, request);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Student updated successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when updating student, id = {}, message = {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when updating student, id = {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Update Student Status", description = "Change student status (ACTIVE/SUSPENDED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ResponseData<StudentResponse>> updateStudentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentStatusRequest request) {
        try {
            log.info("API update student status called, id = {}, status = {}", id, request.getStatus());
            StudentResponse response = studentService.updateStatus(id, request);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Student status updated successfully", response));
        } catch (BusinessException ex) {
            log.warn("Business error when updating student status, id = {}, message = {}", id, ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when updating student status, id = {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Get list user criteria query", description = "Get list user criteria query")
    @GetMapping("/advance-search-by-criteria")
    public ResponseData<?> advanceSearchByCriteria(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String... search
    ){
        log.info("Request get all of users with criteria and sort by and paging");
        return new ResponseData<>(HttpStatus.OK.value(), "students ", studentService.advanceStudents(pageNo, pageSize, sortBy, search));
    }
}