package com.project.library.model;

import com.project.library.utils.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_student")
public class Student extends AbstractEntity<Long> implements Serializable {

    @Column(name = "student_code", unique = true, nullable = false)
    private String studentCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone", unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "major", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Major major;

    @Enumerated(EnumType.STRING)
    @Column(name = "year_of_study")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private YearOfStudy yearOfStudy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private StudentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Gender gender;

    @Column(name = "max_borrow_limit", nullable = false)
    private Integer maxBorrowLimit;

    @OneToOne(mappedBy = "student", fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    /* ===== Business methods ===== */

    public boolean canBorrow() {
        return status == StudentStatus.ACTIVE;
    }

    public long getCurrentBorrowingCount() {
        if(borrowRecords == null) return 0;
        return borrowRecords.stream()
                .filter(r -> r.getStatus().equals(BorrowStatus.BORROWING))
                .count();
    }

    public boolean hasReachedLimit() {
        return getCurrentBorrowingCount() >= maxBorrowLimit;
    }
}
