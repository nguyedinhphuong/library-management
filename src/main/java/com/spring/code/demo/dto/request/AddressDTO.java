package com.spring.code.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AddressDTO implements Serializable {

    @NotBlank(message = "apartmentNumber must not be blank")
    @Size(max = 20, message = "apartmentNumber max 20 characters")
    private String apartmentNumber;

    @NotBlank(message = "floor must not be blank")
    @Pattern(regexp = "\\d+", message = "floor must be numeric")
    private String floor;

    @NotBlank(message = "building must not be blank")
    @Size(max = 100, message = "building max 100 characters")
    private String building;

    @NotBlank(message = "streetNumber must not be blank")
    @Size(max = 20, message = "streetNumber max 20 characters")
    private String streetNumber;

    @NotBlank(message = "street must not be blank")
    @Size(max = 150, message = "street max 150 characters")
    private String street;

    @NotBlank(message = "city must not be blank")
    @Size(max = 100, message = "city max 100 characters")
    private String city;

    @NotBlank(message = "country must not be blank")
    @Size(max = 100, message = "country max 100 characters")
    private String country;

    @NotNull(message = "addressType must not be null")
    @Min(value = 1, message = "addressType must be >= 1")
    @Max(value = 3, message = "addressType must be <= 3")
    private Integer addressType;
}
