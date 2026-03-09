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
 * DTO respons Neraca (Balance Sheet).
 * Rumus: Aset = Liabilitas + Ekuitas.
 * Menyajikan ringkasan posisi keuangan perusahaan pada periode tertentu.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSheetDTO {

    private Instant startDate;
    private Instant endDate;
    private BigDecimal totalAsset;
    private BigDecimal totalLiability;
    private BigDecimal totalEquity;
    private List<BalanceSheetLineDTO> assetAccounts;
    private List<BalanceSheetLineDTO> liabilityAccounts;
    private List<BalanceSheetLineDTO> equityAccounts;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceSheetLineDTO {
        private UUID accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal balance;
    }
}
