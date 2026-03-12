package com.aplikasi.keuangan.controller;

import com.aplikasi.keuangan.dto.DashboardSummaryResponse;
import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import com.aplikasi.keuangan.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
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

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'KASIR')")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        // Extract companyId from JWT SecurityContext to avoid data leak
        UUID companyId = getCompanyIdFromCurrentUser();
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(companyId);
        return ResponseEntity.ok(response);
    }
}
