package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {

    List<JournalLine> findByJournalEntryId(UUID journalEntryId);

    List<JournalLine> findByAccountId(UUID accountId);

    // ──────────────────────────────────────────────
    // Native Query: Agregasi SUM Debit & Kredit
    // dikelompokkan berdasarkan account_id dan account_type
    // dengan filter company_id dan rentang transaction_date
    // ──────────────────────────────────────────────

    @Query(nativeQuery = true, value =
            "SELECT jl.account_id AS accountId, " +
            "       a.account_code AS accountCode, " +
            "       a.account_name AS accountName, " +
            "       a.account_type AS accountType, " +
            "       COALESCE(SUM(jl.debit_amount), 0) AS totalDebit, " +
            "       COALESCE(SUM(jl.credit_amount), 0) AS totalCredit " +
            "FROM journal_lines jl " +
            "JOIN journal_entries je ON jl.journal_entry_id = je.id " +
            "JOIN accounts a ON jl.account_id = a.id " +
            "WHERE je.company_id = :companyId " +
            "  AND je.transaction_date BETWEEN :startDate AND :endDate " +
            "  AND je.deleted_at IS NULL " +
            "GROUP BY jl.account_id, a.account_code, a.account_name, a.account_type " +
            "ORDER BY a.account_code")
    List<Object[]> aggregateByAccountAndType(
            @Param("companyId") UUID companyId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // ──────────────────────────────────────────────
    // Native Query: Detail JournalLine untuk satu akun tertentu
    // (Buku Besar / General Ledger)
    // ──────────────────────────────────────────────

    @Query(nativeQuery = true, value =
            "SELECT je.transaction_date AS transactionDate, " +
            "       je.reference_number AS referenceNumber, " +
            "       je.description AS description, " +
            "       jl.debit_amount AS debitAmount, " +
            "       jl.credit_amount AS creditAmount " +
            "FROM journal_lines jl " +
            "JOIN journal_entries je ON jl.journal_entry_id = je.id " +
            "WHERE je.company_id = :companyId " +
            "  AND jl.account_id = :accountId " +
            "  AND je.transaction_date BETWEEN :startDate AND :endDate " +
            "  AND je.deleted_at IS NULL " +
            "ORDER BY je.transaction_date ASC")
    List<Object[]> findLedgerDetailsByAccount(
            @Param("companyId") UUID companyId,
            @Param("accountId") UUID accountId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // ──────────────────────────────────────────────
    // Native Query: Detail pergerakan Kas/Bank (Arus Kas)
    // Filter berdasarkan account_code yang dimulai dengan '1' (ASSET - Kas/Bank)
    // ──────────────────────────────────────────────

    @Query(nativeQuery = true, value =
            "SELECT jl.account_id AS accountId, " +
            "       a.account_code AS accountCode, " +
            "       a.account_name AS accountName, " +
            "       je.transaction_date AS transactionDate, " +
            "       je.reference_number AS referenceNumber, " +
            "       je.description AS description, " +
            "       jl.debit_amount AS debitAmount, " +
            "       jl.credit_amount AS creditAmount " +
            "FROM journal_lines jl " +
            "JOIN journal_entries je ON jl.journal_entry_id = je.id " +
            "JOIN accounts a ON jl.account_id = a.id " +
            "WHERE je.company_id = :companyId " +
            "  AND je.transaction_date BETWEEN :startDate AND :endDate " +
            "  AND je.deleted_at IS NULL " +
            "  AND (LOWER(a.account_name) LIKE '%kas%' " +
            "       OR LOWER(a.account_name) LIKE '%bank%' " +
            "       OR LOWER(a.account_name) LIKE '%cash%') " +
            "ORDER BY je.transaction_date ASC")
    List<Object[]> findCashFlowMovements(
            @Param("companyId") UUID companyId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}
