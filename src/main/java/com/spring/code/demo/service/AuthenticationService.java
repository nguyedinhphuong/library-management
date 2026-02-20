package com.spring.code.demo.service;

import com.spring.code.demo.dto.request.ResetPasswordDTO;
import com.spring.code.demo.dto.request.SignInRequest;
import com.spring.code.demo.dto.response.TokenResponse;
import com.spring.code.demo.exception.InvalidDataException;
import com.spring.code.demo.model.RedisToken;
import com.spring.code.demo.model.Token;
import com.spring.code.demo.model.User;
import com.spring.code.demo.repository.UserRepository;
import com.spring.code.demo.utils.TokenType;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager  authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenService redisTokenService;
    public TokenResponse accessToken(SignInRequest signInRequest) {

        log.info("--- authenticate ---");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

        var user = userRepository.findByUsername(signInRequest.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Username or password incorrect"));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save token to db khi logout thi xoa di
        tokenService.save(Token.builder().username(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        // save vào redis
//        redisTokenService.save(RedisToken.builder().id(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    public TokenResponse refreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("x-token"); // validate x-token
        if(StringUtils.isBlank(refreshToken)){
            throw new InvalidParameterException("Token must be not blank");
        }

        // extract user from token
        final String userName = jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
        System.out.println("Username: " + userName);
        // check it into database

        var user = userService.getByUsername(userName);

        if(!jwtService.inValid(refreshToken,TokenType.REFRESH_TOKEN, user)){
            throw new InvalidParameterException("Token is invalid");
        }

        String accessToken = jwtService.generateToken(user);

        tokenService.save(Token.builder().username(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        // khi ma accesstoken thi ta van phai luu refreshtoken lai o redis
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    public String removeToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("x-token"); // validate x-token
        if(StringUtils.isBlank(refreshToken)){
            throw new InvalidParameterException("Token must be not blank");
        }

        // luon luon kiem tra co hop le hay khong
        final String userName = jwtService.extractUsername(refreshToken, TokenType.ACCESS_TOKEN);

        // check token in db
        Token currentToken = tokenService.getByUsername(userName);

        // delete token permanent
        tokenService.delete(currentToken);
//        redisTokenService.delete(userName);
        return "Deleted";
    }


    public String forgotPassword(String email) {
        // check email exist or not
        User user = userService.getByEmail(email);

        // User is active or inactive
        if(!user.isEnabled()) throw  new InvalidDataException("Use not active");

        // generate reset token
        String resetToken = jwtService.generateResetToken(user);
        //save to db
        tokenService.save(Token.builder().username(user.getUsername()).resetToken(resetToken).build());
        // luu redis
//        redisTokenService.save(RedisToken.builder().id(user.getUsername()).resetToken(resetToken).build());
        //TODO send email confirm link
        String confirmLink =String.format("curl --location 'http://localhost:8080/auth/refresh-token' \\\n" +
                "--header 'Accept: */*' \\\n" +
                "--header 'Content-Type: application/json' \\\n" +
                "--data '%s'", resetToken) ;
        log.info("confirmLink= {} ", confirmLink);
        return "Sent";
    }

    public String resetPassword(String secretKey) {

        log.info("--- reset password ---");
        final  String userName = jwtService.extractUsername(secretKey, TokenType.RESET_TOKEN);
        var user = userService.getByUsername(userName);
        if(!jwtService.inValid(secretKey, TokenType.RESET_TOKEN, user)){ // check expirydate
            throw new InvalidParameterException("Not allow access with this token");
        }
//        redisTokenService.getById(user.getUsername());
        return "Reset";
    }

    public String changePassword(ResetPasswordDTO request) {
        log.info("--- change password ---");
        User user = isValidUserByToken(request.getSecretKey());
        // cap nhat
        if(!request.getPassword().equals(request.getConfirmPassword())) throw  new InvalidDataException("Password not match");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userService.saveUser(user);

        return "Password changed";
    }

    private User isValidUserByToken(String secretKey) {
        final  String userName = jwtService.extractUsername(secretKey, TokenType.RESET_TOKEN);
        var user = userService.getByUsername(userName);

        if(!user.isEnabled()) throw  new InvalidDataException("Use not active");

        if(!jwtService.inValid(secretKey, TokenType.RESET_TOKEN, user)){ // check expirydate
            throw new InvalidParameterException("Not allow access with this token");
        }
        return user;
    }
}
