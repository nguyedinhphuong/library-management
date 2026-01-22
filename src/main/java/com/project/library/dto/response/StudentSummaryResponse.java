package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;


@Getter
@Builder
@AllArgsConstructor
public class StudentSummaryResponse implements Serializable {
    private Long id;
    private String studentCode;
    private String fullName;
    private String email;
    private String phone;
}
