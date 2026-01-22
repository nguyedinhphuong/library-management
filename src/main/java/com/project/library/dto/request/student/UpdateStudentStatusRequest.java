package com.project.library.dto.request.student;

import com.project.library.utils.EnumSubset;
import com.project.library.utils.StudentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UpdateStudentStatusRequest implements Serializable {

    @NotNull(message = "Status must not be null")
    @EnumSubset(enumClass = StudentStatus.class,
            anyOf = { "ACTIVE", "SUSPENDED" }

    )
    private StudentStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}