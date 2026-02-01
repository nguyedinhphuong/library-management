package com.project.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_student_achievement",uniqueConstraints = @UniqueConstraint(columnNames = {"student_id","achievement_id"}))
public class StudentAchievement extends AbstractEntity<Long> implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "earned_at")
    private LocalDateTime earnAt;

    @Column(name = "progress", nullable = false)
    @Builder.Default
    private Integer progress = 0;

    // mỗi student có 1 tt duy nhất
}
