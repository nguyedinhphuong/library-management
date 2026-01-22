package com.project.library.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumSubsetValidator.class)
public @interface EnumSubset {

    Class<? extends Enum<?>> enumClass();

    String[] anyOf();

    String message() default "must be any of {anyOf}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
