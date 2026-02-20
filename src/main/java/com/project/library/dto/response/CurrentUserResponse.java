package com.project.library.dto.response;

import com.project.library.utils.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentUserResponse {
    private Long userId;
    private String username;
    private UserStatus status;

    // không phải student thì cho null
    private Long studentId;
    private String studentCode;
    private String fullName;
    private String email;
    private String phone;
}
