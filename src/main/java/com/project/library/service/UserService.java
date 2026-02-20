package com.project.library.service;

import com.project.library.dto.response.CurrentUserResponse;
import com.project.library.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {
    UserDetailsService userDetailsService();
    CurrentUserResponse getCurrentUserProfile(User currentUser);
}
