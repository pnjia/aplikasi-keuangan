package com.aplikasi.keuangan.controller;

import com.aplikasi.keuangan.dto.LoginRequest;
import com.aplikasi.keuangan.dto.RegisterRequest;
import com.aplikasi.keuangan.dto.TokenResponse;
import com.aplikasi.keuangan.dto.ErrorResponseDTO;
import com.aplikasi.keuangan.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ErrorResponseDTO> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpStatus.OK.value())
                .message("Logout berhasil")
                .build();
        return ResponseEntity.ok(response);
    }
}

