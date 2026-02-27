package com.project.library.service.impl;

import com.project.library.dto.response.CurrentUserResponse;
import com.project.library.model.User;
import com.project.library.repository.UserRepository;
import com.project.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsernameWithRoles(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUserProfile(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var student = user.getStudent();
        return CurrentUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .studentId(student != null ? student.getId() : null)
                .studentCode(student != null ? student.getStudentCode() : null)
                .fullName(student != null ? student.getFullName() : null)
                .email(student != null ? student.getEmail() : null)
                .phone(student != null ? student.getPhone() : null)
                .build();
    }
}
