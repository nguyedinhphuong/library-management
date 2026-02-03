package com.project.library.repository;

import com.project.library.model.StudentAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAchievementRepository extends JpaRepository<StudentAchievement, Long> {

    List<StudentAchievement> findByStudentId(Long studentId);
    Optional<StudentAchievement> findByStudentIdAndAchievementCode(Long studentId, String code);

    @Query("select sum(sa.achievement.points) from StudentAchievement sa where sa.student.id = :studentId")
    Integer getTotalPointsByStudentId(@Param("studentId") Long studentId);
    boolean existsByStudentIdAndAchievementCode(Long studentId, String code);

}
