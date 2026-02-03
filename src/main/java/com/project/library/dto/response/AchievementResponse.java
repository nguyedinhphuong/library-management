package com.project.library.dto.response;

import com.project.library.utils.AchievementType;
import com.project.library.utils.Rarity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AchievementResponse implements Serializable {

    private Long id;
    private String code;
    private String title;
    private String description;
    private String icon;
    private AchievementType type;
    private Rarity rarity;
    private Integer targetValue;
    private Integer points;
    private LocalDateTime earnedAt;
    private Integer progress;
    private Integer progressPercent;
}
