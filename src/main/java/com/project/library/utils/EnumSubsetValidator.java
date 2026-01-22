package com.project.library.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnumSubsetValidator implements ConstraintValidator<EnumSubset, Enum<?>> {

    private Set<String> acceptedValues;

    @Override
    public void initialize(EnumSubset constraint) {
        acceptedValues = new HashSet<>(Arrays.asList(constraint.anyOf()));
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null để @NotNull xử lý
        }
        return acceptedValues.contains(value.name());
    }
}
