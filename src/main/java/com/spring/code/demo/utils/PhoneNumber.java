package com.spring.code.demo.utils;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented //
@Constraint(validatedBy = PhoneValidator.class) // gọi sang Validator
@Target({ElementType.METHOD, ElementType.FIELD}) // Áp dụng với method hoặc field có thể add nhiều hơn
@Retention(RetentionPolicy.RUNTIME)// Môi trường runtime
public @interface PhoneNumber {

    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
