package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {
    List<JournalLine> findByJournalEntryId(UUID journalEntryId);
    List<JournalLine> findByAccountId(UUID accountId);
}
