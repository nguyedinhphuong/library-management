package com.project.library.utils;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ISBNValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ISBN {

    String message() default "ISBN must be 10-13 digits";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
