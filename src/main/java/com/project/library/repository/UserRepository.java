package com.project.library.repository;

import com.project.library.model.User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.roles ur
            LEFT JOIN FETCH ur.role r
            WHERE u.username = :username
            """)
    Optional<User> findByUsernameWithRoles(String username);
}
