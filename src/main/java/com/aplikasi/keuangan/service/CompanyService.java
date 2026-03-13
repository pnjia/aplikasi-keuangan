package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.CompanyRequest;
import com.aplikasi.keuangan.dto.CompanyResponse;
import com.aplikasi.keuangan.dto.AddMemberRequest;
import com.aplikasi.keuangan.dto.TeamMemberDTO;
import com.aplikasi.keuangan.dto.UpdateTeamMemberRequestDTO;
import com.aplikasi.keuangan.entity.Company;
import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.RoleName;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.CompanyRepository;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyRoleRepository companyRoleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request, UUID userId) {
        Company company = Company.builder()
                .ownerId(userId)
                .name(request.getName())
                .address(request.getAddress())
                .taxNumber(request.getTaxNumber())
                .build();

        Company savedCompany = companyRepository.save(company);

        CompanyRole companyRole = CompanyRole.builder()
                .companyId(savedCompany.getId())
                .userId(userId)
                .roleName(RoleName.OWNER)
                .build();

        companyRoleRepository.save(companyRole);

        return CompanyResponse.builder()
                .id(savedCompany.getId())
                .ownerId(savedCompany.getOwnerId())
                .name(savedCompany.getName())
                .address(savedCompany.getAddress())
                .taxNumber(savedCompany.getTaxNumber())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getUserCompanies(UUID userId) {
        List<CompanyRole> roles = companyRoleRepository.findByUserId(userId);
        
        return roles.stream().map(role -> {
            Company company = companyRepository.findById(role.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            return CompanyResponse.builder()
                    .id(company.getId())
                    .ownerId(company.getOwnerId())
                    .name(company.getName())
                    .address(company.getAddress())
                    .taxNumber(company.getTaxNumber())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamMemberDTO> getTeamMembers(UUID companyId) {
        return companyRoleRepository.findTeamMembersByCompanyId(companyId);
    }

    @Transactional
    public void addTeamMember(UUID companyId, AddMemberRequest request) {
        RoleName roleEnum;
        try {
            roleEnum = RoleName.valueOf(request.getRoleName().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRoleName() + ". Must be one of: OWNER, ADMIN, KASIR");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .fullName(request.getFullName())
                            .email(request.getEmail())
                            .passwordHash(passwordEncoder.encode(request.getPassword()))
                            .build();
                    return userRepository.save(newUser);
                });

        boolean alreadyInTeam = companyRoleRepository
                .findByUserIdAndCompanyIdAndDeletedAtIsNull(user.getId(), companyId)
                .isPresent();

        if (alreadyInTeam) {
            throw new RuntimeException("User sudah menjadi bagian dari tim ini.");
        }

        CompanyRole companyRole = CompanyRole.builder()
                .companyId(companyId)
                .userId(user.getId())
                .roleName(roleEnum)
                .isActive(true)
                .build();

        companyRoleRepository.save(companyRole);
    }

    @Transactional
    public void updateTeamMember(UUID userId, UUID companyId, UpdateTeamMemberRequestDTO request) {
        // Validate new role
        RoleName roleEnum;
        try {
            roleEnum = RoleName.valueOf(request.getRoleName().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRoleName() + ". Must be one of: OWNER, ADMIN, KASIR");
        }

        // Find existing role
        CompanyRole existingRole = companyRoleRepository.findByUserIdAndCompanyIdAndDeletedAtIsNull(userId, companyId)
                .orElseThrow(() -> new RuntimeException("Team member not found in this company"));

        // Prevent downgrading if the only owner
        if (!roleEnum.equals(RoleName.OWNER) && existingRole.getRoleName().equals(RoleName.OWNER)) {
            long ownerCount = companyRoleRepository.countOwnersByCompanyId(companyId);
            if (ownerCount <= 1) {
                throw new RuntimeException("Cannot remove the only owner from the company");
            }
        }

        // Prevent self-deactivation if the only owner
        if (request.getIsActive() != null && !request.getIsActive() && existingRole.getRoleName().equals(RoleName.OWNER)) {
            long ownerCount = companyRoleRepository.countOwnersByCompanyId(companyId);
            if (ownerCount <= 1) {
                throw new RuntimeException("Cannot deactivate the only owner in the company");
            }
        }

        // Update both role AND isActive in one query
        int updatedRows = companyRoleRepository.updateRoleAndStatusByUserIdAndCompanyId(
                userId,
                companyId,
                request.getRoleName().toUpperCase(),
                request.getIsActive() != null ? request.getIsActive() : true
        );
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update team member");
        }
    }

    @Transactional
    public void removeTeamMember(UUID userId, UUID companyId) {
        // Find existing role
        CompanyRole existingRole = companyRoleRepository.findByUserIdAndCompanyIdAndDeletedAtIsNull(userId, companyId)
                .orElseThrow(() -> new RuntimeException("Team member not found in this company"));

        // Prevent removing if the only owner
        if (existingRole.getRoleName().equals(RoleName.OWNER)) {
            long ownerCount = companyRoleRepository.countOwnersByCompanyId(companyId);
            if (ownerCount <= 1) {
                throw new RuntimeException("Cannot remove the only owner from the company");
            }
        }

        // Soft delete the team member
        int deletedRows = companyRoleRepository.softDeleteByUserIdAndCompanyId(userId, companyId, java.time.Instant.now());
        if (deletedRows == 0) {
            throw new RuntimeException("Failed to remove team member");
        }
    }
}
