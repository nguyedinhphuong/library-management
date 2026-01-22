package com.project.library.model;

import com.project.library.utils.UserStatus;
import com.project.library.utils.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_user")
public class User extends AbstractEntity<Long> implements Serializable {

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true)
    private Student student;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}
