package com.project.library.converter;

import com.project.library.dto.response.AchievementResponse;
import com.project.library.model.Achievement;
import com.project.library.model.StudentAchievement;

public class AchievementMapper {

    private AchievementMapper() {}

    public static AchievementResponse toResponse(StudentAchievement achievement) {
        Achievement a = achievement.getAchievement();
        int progressPercent =(int) (((double) achievement.getProgress() /a.getTargetValue()) * 100);
        return AchievementResponse.builder()
                .id(a.getId())
                .code(a.getCode())
                .title(a.getDescription())
                .icon(a.getIcon())
                .type(a.getType())
                .rarity(a.getRarity())
                .targetValue(a.getTargetValue())
                .points(a.getPoints())
                .earnedAt(achievement.getEarnAt())
                .progress(achievement.getProgress())
                .progressPercent(Math.min(100, progressPercent))
                .build();
    }

    public static AchievementResponse toInProgressResponse(Achievement achievement, int progress){
        int progressPercent = (int) (((double) progress / achievement.getTargetValue())* 100);
        return AchievementResponse.builder()
                .id(achievement.getId())
                .code(achievement.getCode())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .icon(achievement.getIcon())
                .type(achievement.getType())
                .rarity(achievement.getRarity())
                .targetValue(achievement.getTargetValue())
                .points(achievement.getPoints())
                .progress(progress)
                .progressPercent(Math.min(100, progressPercent))
                .build();
    }
}
