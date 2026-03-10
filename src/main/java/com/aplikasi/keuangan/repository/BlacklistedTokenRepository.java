package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {

    boolean existsByToken(String token);
}
