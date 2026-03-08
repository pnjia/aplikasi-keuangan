package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.CompanyRequest;
import com.aplikasi.keuangan.dto.CompanyResponse;
import com.aplikasi.keuangan.entity.Company;
import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.RoleName;
import com.aplikasi.keuangan.repository.CompanyRepository;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import lombok.RequiredArgsConstructor;
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
}
