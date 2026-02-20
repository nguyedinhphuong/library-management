package com.project.library.dto.request.auth;

import com.project.library.utils.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SignInRequest {

    @NotBlank(message = "Username must be not blank")
    private String username;

    @NotBlank(message = "Password must be not blank")
    private String password;

    @NotNull(message = "platform must be not null") // có thể tự annotation ...
    private Platform platform;

    private String deviceToken; // api cho mobile ...mỗi mobile có 1 device token nhận 1 device

    private String version;// cho mobile
}
