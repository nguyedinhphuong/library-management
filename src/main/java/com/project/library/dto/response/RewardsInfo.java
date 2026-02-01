package com.project.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RewardsInfo implements Serializable {

    private Integer currentBorrowLimit;
    private Integer baseLimit;
    private Integer bonusFromLevel;
    private Integer bonusFromAchievements;
    private List<String> unlockedPerks;
}
