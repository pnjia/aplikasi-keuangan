package com.aplikasi.keuangan.controller;

import com.aplikasi.keuangan.dto.AccountRequestDTO;
import com.aplikasi.keuangan.dto.AccountResponseDTO;
import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import com.aplikasi.keuangan.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final CompanyRoleRepository companyRoleRepository;

    private UUID getCompanyIdFromCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CompanyRole> roles = companyRoleRepository.findByUserId(user.getId());
        if (roles.isEmpty()) {
            throw new RuntimeException("User does not belong to any company");
        }
        return roles.get(0).getCompanyId();
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@RequestBody AccountRequestDTO request) {
        request.setCompanyId(getCompanyIdFromCurrentUser());
        return new ResponseEntity<>(accountService.createAccount(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'KASIR')")
    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAccounts() {
        UUID companyId = getCompanyIdFromCurrentUser();
        return ResponseEntity.ok(accountService.getAccountsByCompanyId(companyId));
    }

    // ──────────────────────────────────────────────
    // DELETE /api/v1/accounts/{id} — Hapus Akun
    // ──────────────────────────────────────────────
    
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable UUID id) {
        UUID companyId = getCompanyIdFromCurrentUser();
        try {
            accountService.deleteAccount(id, companyId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Akun berhasil dihapus");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ──────────────────────────────────────────────
    // PATCH /api/v1/accounts/{id}/status — Ubah Status Aktif/Nonaktif (Soft-Disable)
    // ──────────────────────────────────────────────
    
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateAccountStatus(@PathVariable UUID id, @RequestBody Map<String, Boolean> request) {
        UUID companyId = getCompanyIdFromCurrentUser();
        Boolean isActive = request.get("isActive");
        
        if (isActive == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Field 'isActive' is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            accountService.updateAccountStatus(id, companyId, isActive);
            Map<String, String> response = new HashMap<>();
            response.put("message", isActive ? "Akun diaktifkan" : "Akun dinonaktifkan");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
