package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    Page<JournalEntry> findByCompanyIdAndDeletedAtIsNull(UUID companyId, Pageable pageable);
    List<JournalEntry> findByCompanyIdAndTransactionDateBetweenAndDeletedAtIsNull(
            UUID companyId, Instant startDate, Instant endDate);
}
