package com.project.library.dto.request.student;

import com.project.library.utils.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UpdateStudentRequest implements Serializable {

    @Size(min = 3, max = 100, message = "Full name must be between 3 to 100 characters")
    private String fullName;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @PhoneNumber
    private String phone;

    private Major major;

    private YearOfStudy yearOfStudy;

    private StudentStatus status;

    private Gender gender;

    @Min(value = 1, message = "Max borrow limit must be at least 1")
    @Max(value = 10, message = "Max borrow limit must not exceed 10")
    private Integer maxBorrowLimit;
}