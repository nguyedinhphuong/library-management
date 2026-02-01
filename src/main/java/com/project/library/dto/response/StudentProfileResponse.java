package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StudentProfileResponse implements Serializable {
    private Long studentId;
    private String studentCode;
    private String fullName;
    private Integer level;
    private Integer totalPoints;
    private Integer currentLevelPoints;
    private Integer nextLevelPoints;
    private Integer processToNextLevel;
    private List<AchievementResponse> earnedAchievement;
    private List<AchievementResponse> inProgress;
    private RewardsInfo rewards;
}
