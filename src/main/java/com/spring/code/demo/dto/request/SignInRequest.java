package com.spring.code.demo.dto.request;

import com.spring.code.demo.utils.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter

public class SignInRequest {

    @NotBlank(message = "username must be not blank")
    private String username;

    @NotBlank(message = "password must be not blank")
    private String password;

    @NotNull(message = "username must be not null")
    private Platform flatform;

    private String deviceToken; //khi viết api cho mobile

    private String version;
}
