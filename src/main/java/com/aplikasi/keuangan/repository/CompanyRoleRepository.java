package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.CompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRoleRepository extends JpaRepository<CompanyRole, UUID> {
    List<CompanyRole> findByUserId(UUID userId);
}
