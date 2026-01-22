package com.project.library.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber,String > {
    @Override
    public void initialize(PhoneNumber constraint) {
    }

    @Override
    public boolean isValid(String phoneNo, ConstraintValidatorContext context) {
        if(phoneNo == null  || phoneNo.isBlank()) return true;
        return phoneNo.matches("\\d{10}")
                || phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")
                || phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")
                || phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}");
    }
}
