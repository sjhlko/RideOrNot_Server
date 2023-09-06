package com.example.hanium2023.controller;

import com.example.hanium2023.domain.dto.user.GoogleIdTokenDTO;
import com.example.hanium2023.service.AuthService;
import com.example.hanium2023.service.VerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signIn")
    public ResponseEntity<String> login(@RequestBody GoogleIdTokenDTO dto) {
        try {
            String googleIdToken = dto.getGoogleIdToken();
            String jwtToken = authService.verifyGoogleIdToken(googleIdToken);
            if (jwtToken != null) {
                // JWT 토큰을 응답 바디에 담아서 반환
                return ResponseEntity.ok(jwtToken);
            } else {
                // 토큰 검증 실패 또는 다른 오류 발생 시 에러 응답
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token verification failed");
            }
        } catch (GeneralSecurityException | IOException e) {
            // 예외 발생 시 에러 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}