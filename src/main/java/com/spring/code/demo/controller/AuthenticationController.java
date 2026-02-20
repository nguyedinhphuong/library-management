package com.spring.code.demo.controller;

import com.spring.code.demo.dto.request.ResetPasswordDTO;
import com.spring.code.demo.dto.request.SignInRequest;
import com.spring.code.demo.dto.response.TokenResponse;
import com.spring.code.demo.service.AuthenticationService;
import com.spring.code.demo.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
@Slf4j
@Tag(name = "Authentication Controller")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    @PostMapping("/access-token")
    public ResponseEntity<TokenResponse> login(@RequestBody SignInRequest request){
        return new ResponseEntity<>(authenticationService.accessToken(request), HttpStatus.OK);
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request){
        return new ResponseEntity<>(authenticationService.refreshToken(request), HttpStatus.OK);
    }
    @PostMapping("/remove-token")
    public ResponseEntity<String> logout(HttpServletRequest request){
        return new ResponseEntity<>(authenticationService.removeToken(request), HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email){
        return new ResponseEntity<>(authenticationService.forgotPassword(email), HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody String secretKey){ // no thuc chat la refresh token
        return new ResponseEntity<>(authenticationService.resetPassword(secretKey), HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO request){ // no thuc chat la refresh token
        return new ResponseEntity<>(authenticationService.changePassword(request), HttpStatus.OK);
    }

}
