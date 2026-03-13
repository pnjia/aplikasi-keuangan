package com.aplikasi.keuangan.controller;

import com.aplikasi.keuangan.dto.CompanyRequest;
import com.aplikasi.keuangan.dto.CompanyResponse;
import com.aplikasi.keuangan.dto.AddMemberRequest;
import com.aplikasi.keuangan.dto.TeamMemberDTO;
import com.aplikasi.keuangan.dto.UpdateTeamMemberRequestDTO;
import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import com.aplikasi.keuangan.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final CompanyRoleRepository companyRoleRepository;

    private User getCurrentUser() {
        // Retrieve email from authentication principal
        // Note: For this to work correctly, an Authentication filter should populate SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UUID getCompanyIdFromCurrentUser() {
        User user = getCurrentUser();
        List<CompanyRole> roles = companyRoleRepository.findByUserId(user.getId());
        if (roles.isEmpty()) {
            throw new RuntimeException("User does not belong to any company");
        }
        return roles.get(0).getCompanyId();
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

    // ──────────────────────────────────────────────
    // GET /api/v1/companies/team — Daftar Anggota Tim (Manajemen Tim)
    // ──────────────────────────────────────────────
    
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'KASIR')")
    @GetMapping("/team")
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembers() {
        UUID companyId = getCompanyIdFromCurrentUser();
        List<TeamMemberDTO> teamMembers = companyService.getTeamMembers(companyId);
        return ResponseEntity.ok(teamMembers);
    }

    // ──────────────────────────────────────────────
    // POST /api/v1/companies/team — Tambah Anggota Tim Baru
    // ──────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PostMapping("/team")
    public ResponseEntity<?> addTeamMember(@RequestBody AddMemberRequest request) {
        UUID companyId = getCompanyIdFromCurrentUser();
        try {
            companyService.addTeamMember(companyId, request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Anggota tim berhasil ditambahkan");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ──────────────────────────────────────────────
    // PUT /api/v1/companies/team/{userId} — Edit Anggota Tim (Ubah Peran & Status)
    // ──────────────────────────────────────────────
    
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PutMapping("/team/{userId}")
    public ResponseEntity<?> updateTeamMember(@PathVariable UUID userId, @RequestBody UpdateTeamMemberRequestDTO request) {
        UUID companyId = getCompanyIdFromCurrentUser();
        try {
            companyService.updateTeamMember(userId, companyId, request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Team member updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ──────────────────────────────────────────────
    // DELETE /api/v1/companies/team/{userId} — Keluarkan Anggota Tim
    // ──────────────────────────────────────────────
    
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @DeleteMapping("/team/{userId}")
    public ResponseEntity<?> removeTeamMember(@PathVariable UUID userId) {
        UUID companyId = getCompanyIdFromCurrentUser();
        try {
            companyService.removeTeamMember(userId, companyId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Team member removed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
