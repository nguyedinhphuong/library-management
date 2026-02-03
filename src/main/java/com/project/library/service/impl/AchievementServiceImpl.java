package com.project.library.service.impl;

import com.project.library.converter.AchievementMapper;
import com.project.library.dto.response.AchievementResponse;
import com.project.library.dto.response.RewardsInfo;
import com.project.library.dto.response.StudentProfileResponse;
import com.project.library.exception.BusinessException;
import com.project.library.model.Achievement;
import com.project.library.model.Student;
import com.project.library.model.StudentAchievement;
import com.project.library.repository.AchievementRepository;
import com.project.library.repository.BorrowRecordRepository;
import com.project.library.repository.StudentAchievementRepository;
import com.project.library.repository.StudentRepository;
import com.project.library.service.AchievementService;
import com.project.library.utils.AchievementType;
import com.project.library.utils.BorrowStatus;
import com.project.library.utils.Major;
import com.project.library.utils.Rarity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final StudentRepository studentRepository;
    private final AchievementRepository achievementRepository;
    private final StudentAchievementRepository studentAchievementRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    // level sys
    private static final int POINT_PER_LEVEL = 100;
    private static final int BASE_BORROW_LIMIT = 5;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "studentProfile", key = "#studentId")
    public StudentProfileResponse getStudentProfile(Long studentId) {
        log.debug("Get student profile with achievements - studentId: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Student not found"));

        // tinh points
        Integer totalPoints = studentAchievementRepository.getTotalPointsByStudentId(studentId);
        if (totalPoints == null) totalPoints = 0;

        // level
        int level = calculateLevel(totalPoints);
        int currentLevelPoints = level * level * POINT_PER_LEVEL;
        int nexLevelPoints = (level + 1) * (level + 1) * POINT_PER_LEVEL;
        int progressToNextLevel = (int) (((double) (totalPoints - currentLevelPoints) / (nexLevelPoints - currentLevelPoints)) * 100);

        //get earned
        List<StudentAchievement> studentAchievements = studentAchievementRepository.findByStudentId(studentId);
        List<AchievementResponse> earnedAchievements = studentAchievements.stream()
                .filter(sa -> sa.getProgress() >= sa.getAchievement().getTargetValue())
                .map(AchievementMapper::toResponse)
                .sorted((a, b) -> b.getEarnedAt().compareTo(a.getEarnedAt()))
                .toList();

        // check co achieve chua ?
        List<Achievement> allAchievements = achievementRepository.findByIsActiveTrue();
        List<AchievementResponse> inProgress = new ArrayList<>();

        for (Achievement achievement : allAchievements) {
            //sv có tiến độ ?
            Optional<StudentAchievement> existing = studentAchievementRepository.findByStudentIdAndAchievementCode(studentId, achievement.getCode());
            if (existing.isEmpty() || existing.get().getProgress() < achievement.getTargetValue()) {
                int progress = existing.map(StudentAchievement::getProgress).orElse(0);
                inProgress.add(AchievementMapper.toInProgressResponse(achievement, progress));
            }
        }

        // cal rewards
        RewardsInfo rewards = calculateRewards(student, level, earnedAchievements.size());

        return StudentProfileResponse.builder()
                .studentId(studentId)
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .level(level)
                .totalPoints(totalPoints)
                .currentLevelPoints(currentLevelPoints)
                .nextLevelPoints(nexLevelPoints)
                .processToNextLevel(Math.max(0, Math.min(100, progressToNextLevel)))
                .earnedAchievement(earnedAchievements)
                .inProgress(inProgress)
                .rewards(rewards)
                .build();
    }

    @Override
    @Transactional
//    @Async
    public void checkAndUnlockAchievements(Long studentId) {
        log.debug("Checking achievements for student: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElse(null);

        if(student == null) return;
        checkBooksReadAchievements(student);
        checkPerfectRecordAchievements(student);
        checkCategoryMasterAchievements(student);
        checkSpeedReaderAchievements(student);
    }

    @Override
    @Transactional
    public void initializeDefaultAchievements() {
        log.info("Initialize default achievements");
        List<Achievement> achievements = Arrays.asList(
                Achievement.builder().code("FIRST_BOOK").title("First Steps").description("Borrow your first book").icon("📘").type(AchievementType.BOOKS_READ).rarity(Rarity.COMMON).targetValue(1).points(10).isActive(true).build(),
                Achievement.builder().code("BOOKWORM").title("Bookworm").description("Read 50 books").icon("📚").type(AchievementType.BOOKS_READ).rarity(Rarity.EPIC).targetValue(50).points(500).isActive(true).build(),
                Achievement.builder().code("LIBRARY_LEGEND").title("Library Legend").description("Read 100 books").icon("👑").type(AchievementType.BOOKS_READ).rarity(Rarity.LEGENDARY).targetValue(100).points(1000).isActive(true).build(),
                Achievement.builder().code("PUNCTUAL").title("Always On Time").description("Return 10 books without overdue").icon("⏱").type(AchievementType.PERFECT_RECORD).rarity(Rarity.RARE).targetValue(10).points(200).isActive(true).build(),
                Achievement.builder().code("PERFECT_RECORD").title("Perfect Record").description("Return 50 books without any overdue").icon("★").type(AchievementType.PERFECT_RECORD).rarity(Rarity.EPIC).targetValue(50).points(500).isActive(true).build(),
                Achievement.builder().code("SPEED_READER").title("Speed Reader").description("Read 5 books in one month").icon("⚡").type(AchievementType.SPEED_READER).rarity(Rarity.RARE).targetValue(5).points(300).isActive(true).build(),
                Achievement.builder().code("IT_MASTER").title("IT Master").description("Read 10 IT books").icon("💻").type(AchievementType.CATEGORY_MASTER).rarity(Rarity.RARE).targetValue(10).points(250).isActive(true).build()
        );
        for(Achievement achievement : achievements) {
            if(!achievementRepository.findByCode(achievement.getCode()).isPresent()){
                achievementRepository.save(achievement);
            }
        }
        log.info("Initialized {} achievements", achievements.size());
    }

    private int calculateLevel(int totalPoints) {
        return (int) Math.floor(Math.sqrt((double) totalPoints / POINT_PER_LEVEL));
    }

    private RewardsInfo calculateRewards(Student student, int level, int achievementCount) {
        int bonusFromLevel = level / 5; // +1 borrow
        int bonusFromAchievements = achievementCount / 10;
        List<String> peeks = new ArrayList<>();
        if (level >= 5) peeks.add("Priority book reservation");
        if (level >= 10) peeks.add("Extended borrow period(+7 days)");
        if (level >= 15) peeks.add("VIP lounge access");
        if (achievementCount >= 5) peeks.add("Exclusive book recommendations");
        return RewardsInfo.builder()
                .currentBorrowLimit(student.getMaxBorrowLimit())
                .baseLimit(BASE_BORROW_LIMIT)
                .bonusFromLevel(bonusFromLevel)
                .bonusFromAchievements(bonusFromAchievements)
                .unlockedPerks(peeks)
                .build();
    }

    private void checkBooksReadAchievements(Student student) {
        long totalReturned = borrowRecordRepository.countByStudentIdAndStatus(student.getId(), BorrowStatus.RETURNED);
        updateAchievementProgress(student.getId(), "FIRST_BOOK", (int) totalReturned);
        updateAchievementProgress(student.getId(), "BOOKWORM", (int) totalReturned);
        updateAchievementProgress(student.getId(), "LIBRARY_LEGEND", (int) totalReturned);
    }
    private void checkPerfectRecordAchievements(Student student) {
        long onTimeReturns = borrowRecordRepository.countOnTimeReturns(student.getId());
        updateAchievementProgress(student.getId(), "PUNCTUAL", (int) onTimeReturns);
        updateAchievementProgress(student.getId(), "PERFECT_RECORD", (int) onTimeReturns);
    }
    private void checkCategoryMasterAchievements(Student student) {
        // Count IT books borrowed
        long itBooks = borrowRecordRepository.countByStudentAndCategory(
                student.getId(), String.valueOf(Major.INFORMATION_TECHNOLOGY));

        updateAchievementProgress(student.getId(), "IT_MASTER", (int) itBooks);
    }
    private void checkSpeedReaderAchievements(Student student) {
        // Count books this month
        long thisMonth = borrowRecordRepository.countBorrowsThisMonth(student.getId());

        updateAchievementProgress(student.getId(), "SPEED_READER", (int) thisMonth);
    }
    @Transactional
    private void updateAchievementProgress(Long studentId, String achievementCode, int progress) {
        StudentAchievement sa = studentAchievementRepository.findByStudentIdAndAchievementCode(studentId, achievementCode)
                .orElseThrow(() ->
                new IllegalStateException(
                        "StudentAchievement not initialized for studentId=" + studentId
                )
        );
        if(sa.getEarnAt() != null) return; // đã unlock thì k xử lý
        sa.setProgress(progress);
        if(progress >= sa.getAchievement().getTargetValue()){
            sa.setEarnAt(LocalDateTime.now());
        }
    }
//    private void updateAchievementProgress(Long studentId, String achievementCode, int progress) {
//        // check  achievement
//        Optional<Achievement> achievementOpt = achievementRepository.findByCode(achievementCode);
//        if(achievementOpt.isEmpty()) return;
//        // kiểm tra sv có achievement chưa
//
//        Achievement achievement = achievementOpt.get();
//        Optional<StudentAchievement> existingOpt  = studentAchievementRepository.findByStudentIdAndAchievementCode(studentId, achievementCode );
//        if(existingOpt.isPresent()) {
//            StudentAchievement existing = existingOpt.get();
//            if(existing.getProgress() < progress) {
//                existing.setProgress(Math.min(progress, achievement.getTargetValue()));
//                // unlock
//                if(progress >= achievement.getTargetValue() && existing.getEarnAt() == null){
//                    existing.setEarnAt(LocalDateTime.now());
//                    log.info("Achievement unlocked! Student {} earned: {}",
//                            studentId, achievement.getTitle());
//                }
//                studentAchievementRepository.save(existing);
//            }
//        }else{
//            StudentAchievement newProgress = StudentAchievement.builder()
//                    .student(studentRepository.findById(studentId).orElse(null))
//                    .achievement(achievement)
//                    .progress(Math.min(progress, achievement.getTargetValue()))
//                    .earnAt(progress >= achievement.getTargetValue() ? LocalDateTime.now() : null)
//                    .build();
//            studentAchievementRepository.save(newProgress);
//        }
//    }

}
