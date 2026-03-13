package com.aplikasi.keuangan.config;

import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.BlacklistedTokenRepository;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Filter JWT yang dieksekusi satu kali per request.
 *
 * Tugas filter ini:
 * 1. Membaca token JWT dari header "Authorization: Bearer <token>"
 * 2. Mengecek apakah token ada di daftar hitam (blacklisted_tokens)
 * 3. Memvalidasi token menggunakan JwtUtil
 * 4. Mengambil email pengguna dari token
 * 5. Mengambil peran (OWNER/ADMIN/KASIR) dari tabel company_roles
 * 6. Menanamkan Authentication + Authorities ke SecurityContext
 *    agar @PreAuthorize dapat bekerja dengan benar
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CompanyRoleRepository companyRoleRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractTokenFromHeader(request);

            if (jwt != null) {
                // ATURAN MUTLAK: Cek daftar hitam SEBELUM validasi token
                if (blacklistedTokenRepository.existsByToken(jwt)) {
                    log.warn("Token ditolak: token sudah ada di daftar hitam (blacklisted)");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"timestamp\":\"" + java.time.ZonedDateTime.now() + "\","
                          + "\"status\":401,"
                          + "\"error\":\"Unauthorized\","
                          + "\"message\":\"Token sudah tidak valid. Silakan login kembali.\"}"
                    );
                    return; // Hentikan filter chain, jangan lanjut
                }

                if (jwtUtil.validateToken(jwt)) {
                    String email = jwtUtil.getUserEmailFromToken(jwt);

                    Optional<User> userOptional = userRepository.findByEmail(email);

                    if (userOptional.isPresent()) {
                        User user = userOptional.get();

                        // Ambil semua peran pengguna dari tabel company_roles
                        // Jika terjadi gangguan query (misalnya schema mismatch sementara),
                        // jangan matikan seluruh konteks keamanan.
                        List<CompanyRole> roles = new ArrayList<>();
                        try {
                            roles = companyRoleRepository.findByUserId(user.getId());
                        } catch (Exception roleLoadException) {
                            log.error("Gagal mengambil role user {}: {}", email, roleLoadException.getMessage());
                        }

                        // Konversi peran menjadi GrantedAuthority untuk @PreAuthorize
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        for (CompanyRole role : roles) {
                            authorities.add(new SimpleGrantedAuthority(role.getRoleName().name()));
                        }

                        // Buat objek autentikasi
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Tanamkan ke SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Authenticated user: {} with roles: {}", email, authorities);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Mengekstrak token JWT dari header "Authorization: Bearer <token>".
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}

