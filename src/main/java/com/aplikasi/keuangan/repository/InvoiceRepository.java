package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findByCompanyIdAndDeletedAtIsNull(UUID companyId, Pageable pageable);
    Page<Invoice> findByCompanyIdAndContactIdAndDeletedAtIsNull(UUID companyId, UUID contactId, Pageable pageable);
    boolean existsByContactIdAndDeletedAtIsNull(UUID contactId);
}
