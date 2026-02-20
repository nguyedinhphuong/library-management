package com.project.library.service;

import com.project.library.dto.request.auth.SignInRequest;
import com.project.library.dto.response.TokenResponse;
import com.project.library.exception.BusinessException;
import com.project.library.exception.InvalidDataException;
import com.project.library.model.Token;
import com.project.library.model.User;
import com.project.library.repository.UserRepository;
import com.project.library.utils.TokenType;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;

    public TokenResponse authenticate(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Username or password is correct"));
        String accessToken = jwtService.generateToken(user);

        String refreshToken = jwtService.generateRefreshToken(user);
// save token to db
        tokenService.save(Token.builder()
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    public TokenResponse refresh(HttpServletRequest request) {
        String refreshToken = request.getHeader("x-token");

        // validate x-token
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataException("Token must be not blank");
        }

        // extract user tuừ token

        final String userName = jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
        log.info("username {}", userName);
        // check into db
        Optional<User> user = userRepository.findByUsername(userName);
        System.out.println("userId: " + user.get().getId());

        // validate
        if (!jwtService.isValid(refreshToken, user.get(), TokenType.REFRESH_TOKEN)) {
            throw new InvalidDataException("Token invalid");
        }

        // gen ra new access token
        String accessToken = jwtService.generateToken(user.get());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.get().getId())
                .build();
    }

    public String logout(HttpServletRequest request) {
        String refreshToken = request.getHeader("x-token");

        // validate x-token
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataException("Token must be not blank");
        }
        final String username = jwtService.extractUsername(refreshToken, TokenType.ACCESS_TOKEN);
        Token currentToken = tokenService.getByUsername(username);
        tokenService.delete(currentToken);
        return "Deleted";
    }
}
