package com.project.library.controller;


import com.project.library.dto.response.ResponseData;
import com.project.library.dto.response.StudentProfileResponse;
import com.project.library.exception.BusinessException;
import com.project.library.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/achievements")
@Slf4j
@Tag(name = "Achievement Controller")
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;

    @Operation(summary = "Get Student Profile with Achievements",
            description = "Get gamification profile including level, points, achievements, and rewards")
    @GetMapping("/students/{id}/profile")
    public ResponseEntity<ResponseData<StudentProfileResponse>> getStudentProfile(@PathVariable Long id) {
        try {
            log.debug("API get student profile called, studentId: {}", id);
            StudentProfileResponse response = achievementService.getStudentProfile(id);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(),
                            String.format("Level %d - %d points - %d achievements unlocked",
                                    response.getLevel(), response.getTotalPoints(),
                                    response.getEarnedAchievement().size()),
                            response));
        } catch (BusinessException ex) {
            log.warn("Business error when getting student profile, message: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error when getting student profile", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
    @Operation(summary = "Trigger Achievement Check",
            description = "Manually trigger achievement check for a student (usually called after borrow/return)")
    @PostMapping("/students/{id}/check")
    public ResponseEntity<ResponseData<Void>> checkAchievements(@PathVariable Long id) {
        try {
            log.info("API check achievements called, studentId: {}", id);
            achievementService.checkAndUnlockAchievements(id);
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Achievement check triggered"));
        } catch (Exception ex) {
            log.error("Error when checking achievements", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }

    @Operation(summary = "Initialize Default Achievements",
            description = "Admin endpoint to create default achievements (run once)")
    @PostMapping("/initialize")
    public ResponseEntity<ResponseData<Void>> initializeAchievements() {
        try {
            log.info("API initialize achievements called");
            achievementService.initializeDefaultAchievements();
            return ResponseEntity.ok(
                    new ResponseData<>(HttpStatus.OK.value(), "Default achievements initialized"));
        } catch (Exception ex) {
            log.error("Error when initializing achievements", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
        }
    }
}
