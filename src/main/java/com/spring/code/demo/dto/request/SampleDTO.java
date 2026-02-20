package com.spring.code.demo.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter(AccessLevel.PROTECTED)
public class SampleDTO implements Serializable {
    private Integer id;
    private String name;
}
