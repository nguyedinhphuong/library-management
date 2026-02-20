package com.project.library.service;

import com.project.library.model.User;
import com.project.library.utils.TokenType;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(UserDetails user);
    String extractUsername(String token, TokenType type);

    boolean isValid(String token, UserDetails user, TokenType type);

    String generateRefreshToken(User user);
}
