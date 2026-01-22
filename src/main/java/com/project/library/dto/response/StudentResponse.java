package com.project.library.dto.response;

import com.project.library.utils.Gender;
import com.project.library.utils.Major;
import com.project.library.utils.StudentStatus;
import com.project.library.utils.YearOfStudy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String studentCode;
    private String fullName;
    private String email;
    private String phone;
    private Major major;
    private YearOfStudy yearOfStudy;
    private StudentStatus status;
    private Gender gender;
    private Integer maxBorrowLimit;
    private Long currentBorrowingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
