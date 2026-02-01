package com.project.library.service;

import com.project.library.dto.response.StudentProfileResponse;

public interface AchievementService {

    StudentProfileResponse getStudentProfile(Long studentId);
    void checkAndUnlockAchievements(Long studentId);
    void initializeDefaultAchievements();
}
