package com.project.library.dto.response;

import com.project.library.model.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class StudentRankingResponse implements Serializable {
    private Integer rank;
    private String studentCode;
    private String fullName;
    private String email;
    private Long totalBookRead;
    private Long overdueCount;
    private Boolean neverOverdue;
}
