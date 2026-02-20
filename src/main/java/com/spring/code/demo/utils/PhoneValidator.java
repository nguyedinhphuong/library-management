package com.spring.code.demo.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;

public class PhoneValidator implements ConstraintValidator<PhoneNumber,String> {
    @Override
    public void initialize(PhoneNumber phoneNumberNo) {
    }
    @Override
    public boolean isValid(String phoneNo, ConstraintValidatorContext context) {
        if (phoneNo == null || phoneNo.isBlank()) {
            return true; // để @NotBlank xử lý
        }
        return phoneNo.matches("\\d{10}")
                || phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")
                || phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")
                || phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}");
    }

}
