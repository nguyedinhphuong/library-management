package com.project.library.dto.request.student;

import com.project.library.utils.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CreateStudentRequest implements Serializable {

    @NotBlank(message = "Full name must not be blank")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email has invalid format")
    private String email;

    @NotBlank(message = "Phone number must not be blank")
    @PhoneNumber
    private String phone;

    @NotNull(message = "Major must not be null")
    @EnumSubset(
            enumClass = Major.class,
            anyOf = {"INFORMATION_TECHNOLOGY", "COMPUTER_SCIENCE", "SOFTWARE_ENGINEERING", "INFORMATION_SYSTEMS", "DATA_SCIENCE", "CYBER_SECURITY",
                    "ARTIFICIAL_INTELLIGENCE", "BUSINESS_ADMINISTRATION", "ACCOUNTING", "FINANCE", "MARKETING", "ECONOMICS"}
    )
    private Major major;

    @NotNull(message = "Year of study must not be null")
    @EnumSubset(
            enumClass = YearOfStudy.class,
            anyOf = {"YEAR_1", "YEAR_2", "YEAR_3", "YEAR_4"}
    )
    private YearOfStudy yearOfStudy;

    /**
     * Khi tạo mới student → chỉ được ACTIVE
     */
    @NotNull(message = "Student status must not be null")
    @EnumSubset(
            enumClass = StudentStatus.class,
            anyOf = { "ACTIVE" },
            message = "New student status must be ACTIVE"
    )
    private StudentStatus status;

    @NotNull(message = "Gender must not be null")
    @EnumSubset(
            enumClass = Gender.class,
            anyOf = { "MALE", "FEMALE", "OTHER" }
    )
    private Gender gender;


    @NotNull(message = "Max borrow limit must not be null")
    @Min(value = 1, message = "Max borrow limit must be at least 1")
    @Max(value = 10, message = "Max borrow limit must not exceed 10")
    private Integer maxBorrowLimit;
}
