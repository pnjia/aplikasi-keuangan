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
 * DTO respons Jurnal Umum (General Journal).
 * Menampilkan daftar entri jurnal beserta baris debit/kredit-nya
 * dalam rentang waktu tertentu.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalReportDTO {

    private Instant startDate;
    private Instant endDate;
    private List<JournalEntryDetail> entries;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JournalEntryDetail {
        private UUID journalEntryId;
        private Instant transactionDate;
        private String referenceNumber;
        private String description;
        private List<JournalLineDetail> lines;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JournalLineDetail {
        private UUID accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}
