package com.spring.code.demo.service;

import com.spring.code.demo.utils.TokenType;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(UserDetails user);
    String generateRefreshToken(UserDetails user);
    String generateResetToken(UserDetails user);
    String extractUsername(String token, TokenType type);

    boolean inValid(String token,TokenType type, UserDetails user);
}
