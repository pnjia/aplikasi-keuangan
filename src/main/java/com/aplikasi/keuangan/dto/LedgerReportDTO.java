package com.aplikasi.keuangan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO respons Buku Besar (General Ledger).
 * Menampilkan riwayat pergerakan satu akun tertentu
 * beserta saldo berjalan (running balance).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerReportDTO {

    private UUID accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private Instant startDate;
    private Instant endDate;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal balance;
    private List<LedgerLineDetail> lines;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LedgerLineDetail {
        private Instant transactionDate;
        private String referenceNumber;
        private String description;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private BigDecimal runningBalance;
    }
}
