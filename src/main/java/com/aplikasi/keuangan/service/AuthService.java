package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.config.JwtUtil;
import com.aplikasi.keuangan.dto.LoginRequest;
import com.aplikasi.keuangan.dto.RegisterRequest;
import com.aplikasi.keuangan.dto.TokenResponse;
import com.aplikasi.keuangan.entity.BlacklistedToken;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.BlacklistedTokenRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return TokenResponse.builder().token(token).build();
    }

    public void logout(String token) {
        // Ekstrak waktu kedaluwarsa dari klaim JWT
        Date expiration = jwtUtil.getExpirationFromToken(token);
        ZonedDateTime expiresAt = expiration.toInstant().atZone(ZoneId.systemDefault());

        // Simpan token ke daftar hitam
        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();

        blacklistedTokenRepository.save(blacklistedToken);

        // Bersihkan konteks keamanan
        SecurityContextHolder.clearContext();
    }
}

