package com.project.library.dto.request.student;

import com.project.library.utils.Gender;
import com.project.library.utils.Major;
import com.project.library.utils.PhoneNumber;
import com.project.library.utils.YearOfStudy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BulkImportStudentRequest implements Serializable {

    @NotBlank(message = "Full name must not be blank")
    @Size(min = 3, max = 100, message = "Full name must be between 3 to 100 characters")
    private String fullName;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone must not be blank")
    @PhoneNumber
    private String phone;

    @NotNull(message = "Major must not be null")
    private Major major;

    @NotNull(message = "Year of study must not be null")
    private YearOfStudy yearOfStudy;
    private Gender gender;
}
