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
 * DTO respons Neraca Saldo (Trial Balance).
 * Menampilkan ringkasan total Debit dan Kredit per akun
 * untuk membuktikan keseimbangan buku besar.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrialBalanceDTO {

    private Instant startDate;
    private Instant endDate;
    private BigDecimal grandTotalDebit;
    private BigDecimal grandTotalCredit;
    private List<TrialBalanceLineDTO> accounts;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrialBalanceLineDTO {
        private UUID accountId;
        private String accountCode;
        private String accountName;
        private String accountType;
        private BigDecimal totalDebit;
        private BigDecimal totalCredit;
    }
}
