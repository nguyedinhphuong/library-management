package com.project.library.converter;

import com.project.library.dto.request.student.CreateStudentRequest;
import com.project.library.dto.request.student.UpdateStudentRequest;
import com.project.library.dto.response.StudentResponse;
import com.project.library.model.Student;
import com.project.library.utils.StudentStatus;

public class StudentMapper {

    private StudentMapper () {}

    public static Student toEntity(CreateStudentRequest request, String studentCode) {
        return Student.builder()
                .studentCode(studentCode)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .major(request.getMajor())
                .yearOfStudy(request.getYearOfStudy())
                .gender(request.getGender())
                .status(StudentStatus.ACTIVE)
                .maxBorrowLimit(request.getMaxBorrowLimit() != null ? request.getMaxBorrowLimit() : 5 )
                .build();
    }

    public static StudentResponse toResponse(Student student){
        return StudentResponse.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .major(student.getMajor())
                .yearOfStudy(student.getYearOfStudy())
                .status(student.getStatus())
                .gender(student.getGender())
                .maxBorrowLimit(student.getMaxBorrowLimit())
                .currentBorrowingCount(student.getCurrentBorrowingCount())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    public static void updateEntity(Student student, UpdateStudentRequest request) {
        if (request.getFullName() != null) {
            student.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            student.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        if (request.getMajor() != null) {
            student.setMajor(request.getMajor());
        }
        if (request.getYearOfStudy() != null) {
            student.setYearOfStudy(request.getYearOfStudy());
        }
        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }
        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }
        if (request.getMaxBorrowLimit() != null) {
            student.setMaxBorrowLimit(request.getMaxBorrowLimit());
        }
    }
}
