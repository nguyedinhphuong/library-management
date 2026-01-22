package com.project.library.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ISBNValidator implements ConstraintValidator<ISBN, String> {

    private static final String ISBN_REGEX = "^[0-9]{10,13}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null) return true;
        return value.matches(ISBN_REGEX);
    }
}
