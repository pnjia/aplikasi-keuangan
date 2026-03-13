package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.dto.TeamMemberDTO;
import com.aplikasi.keuangan.entity.CompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRoleRepository extends JpaRepository<CompanyRole, UUID> {
    List<CompanyRole> findByUserId(UUID userId);

    // Query untuk mendapatkan daftar tim di satu perusahaan
    // JOIN antara users dan company_roles berdasarkan companyId
    @Query("""
            SELECT new com.aplikasi.keuangan.dto.TeamMemberDTO(
                u.id,
                u.fullName,
                u.email,
                CAST(cr.roleName AS string),
                cr.isActive
            )
            FROM CompanyRole cr
            JOIN User u ON cr.userId = u.id
            WHERE cr.companyId = :companyId
            AND cr.deletedAt IS NULL
            ORDER BY u.fullName ASC
            """)
    List<TeamMemberDTO> findTeamMembersByCompanyId(@Param("companyId") UUID companyId);

    // Query untuk cek apakah user sudah memiliki role di company tertentu
    Optional<CompanyRole> findByUserIdAndCompanyIdAndDeletedAtIsNull(UUID userId, UUID companyId);

    // Query untuk count OWNER di company tertentu (utk validasi)
    @Query("""
            SELECT COUNT(cr) FROM CompanyRole cr
            WHERE cr.companyId = :companyId
            AND cr.roleName = com.aplikasi.keuangan.entity.RoleName.OWNER
            AND cr.deletedAt IS NULL
            """)
    long countOwnersByCompanyId(@Param("companyId") UUID companyId);

    // Update role untuk user di company tertentu
    @Modifying
    @Query("""
            UPDATE CompanyRole cr
            SET cr.roleName = CAST(:newRole AS com.aplikasi.keuangan.entity.RoleName)
            WHERE cr.userId = :userId
            AND cr.companyId = :companyId
            AND cr.deletedAt IS NULL
            """)
    int updateRoleByUserIdAndCompanyId(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId,
            @Param("newRole") String newRole
    );

    // Update role AND isActive untuk team member (consolidated endpoint)
    @Modifying
    @Query("""
            UPDATE CompanyRole cr
            SET cr.roleName = CAST(:roleName AS com.aplikasi.keuangan.entity.RoleName),
                cr.isActive = :isActive
            WHERE cr.userId = :userId
            AND cr.companyId = :companyId
            AND cr.deletedAt IS NULL
            """)
    int updateRoleAndStatusByUserIdAndCompanyId(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId,
            @Param("roleName") String roleName,
            @Param("isActive") Boolean isActive
    );

    // Soft delete: set deletedAt untuk mengeluarkan user dari company (audit trail preserved)
    @Modifying
    @Query("""
            UPDATE CompanyRole cr
            SET cr.deletedAt = :deletedAt
            WHERE cr.userId = :userId
            AND cr.companyId = :companyId
            AND cr.deletedAt IS NULL
            """)
    int softDeleteByUserIdAndCompanyId(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId,
            @Param("deletedAt") Instant deletedAt
    );
}
