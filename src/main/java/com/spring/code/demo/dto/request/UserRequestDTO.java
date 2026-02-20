package com.spring.code.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spring.code.demo.utils.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@Getter
@Setter
public class UserRequestDTO implements Serializable {

    @NotBlank(message = "name must be not blank")
    private String firstName;

    @NotBlank(message = "name must be not blank")
    private String lastName;

    @NotBlank(message = "username must be not blank")
    private String username;

    @NotBlank(message = "Password must be not blank")
    private String password;

    @NotBlank(message = "email must be not blank")
    @Email(message = "email invalid format")
    private String email;

//    @Pattern(regexp = "^\\d{10}$", message = "phone invalid format")
    @NotBlank(message = "phone must be not blank")
    @PhoneNumber
    private String phone;

    @NotNull(message = "dateOfBirth must be not null")
    private LocalDate dateOfBirth;

    @NotEmpty(message = "permission must not be empty")
    List<@NotBlank  String> permission;

//    @Pattern(regexp = "^ACTIVE|INACTIVE|NONE$", message = "status must be one in {ACTIVE, INACTIVE, NONE}")
    // Nên viết 1 Annotation để xử lý
    // Cách 1
    @EnumPattern(name="status", regexp = "ACTIVE|INACTIVE|NONE")
    private UserStatus status;

    // Cách 2
    @GenderSubset(anyOf = {Gender.MALE, Gender.FEMALE, Gender.OTHER})
    private Gender gender;

    // Cách 3 kiểu đối chiếu String
    @NotNull(message = "type must be not null")
    @EnumValue(name="type", enumClass = UserType.class)
    private String type;

    @NotEmpty(message = "address is not empty")
    public Set<AddressDTO> address;
    
    public UserRequestDTO(String firstName, String lastName, String email, String phone, List<@NotBlank String> permission) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.permission = permission;
    }
}
