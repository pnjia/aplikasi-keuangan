package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
}
