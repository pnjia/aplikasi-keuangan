package com.aplikasi.keuangan.controller;

import com.aplikasi.keuangan.dto.CompanyRequest;
import com.aplikasi.keuangan.dto.CompanyResponse;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.UserRepository;
import com.aplikasi.keuangan.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        // Retrieve email from authentication principal
        // Note: For this to work correctly, an Authentication filter should populate SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Endpoint ini TIDAK menggunakan @PreAuthorize karena user baru yang baru register
    // belum memiliki peran apapun. Setelah membuat perusahaan, user otomatis menjadi OWNER
    // melalui logika di CompanyService.createCompany().
    // Keamanan tetap terjaga karena endpoint ini dilindungi oleh .authenticated() di SecurityConfig.
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@RequestBody CompanyRequest request) {
        User user = getCurrentUser();
        return new ResponseEntity<>(companyService.createCompany(request, user.getId()), HttpStatus.CREATED);
    }

    // Endpoint ini TIDAK menggunakan @PreAuthorize karena user baru yang belum punya
    // perusahaan tetap harus bisa memanggil endpoint ini (akan mendapat daftar kosong).
    // Keamanan tetap terjaga karena dilindungi oleh .authenticated() di SecurityConfig.
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getCompanies() {
        User user = getCurrentUser();
        return ResponseEntity.ok(companyService.getUserCompanies(user.getId()));
    }
}
