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
 * Menyajikan ringkasan posisi keuangan perusahaan pada periode tertentu,
 * dikelompokkan menjadi 3 blok utama: Aset, Kewajiban, dan Ekuitas.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSheetDTO {

    private Instant startDate;
    private Instant endDate;

    // ── Blok Aset ──
    private List<BalanceSheetLineDTO> assetAccounts;
    private BigDecimal totalAssets;

    // ── Blok Kewajiban ──
    private List<BalanceSheetLineDTO> liabilityAccounts;
    private BigDecimal totalLiabilities;

    // ── Blok Ekuitas (termasuk Laba Berjalan) ──
    private List<BalanceSheetLineDTO> equityAccounts;
    private BigDecimal totalEquities;

    // ── Grand Total: Kewajiban + Ekuitas ──
    private BigDecimal totalLiabilitiesAndEquities;

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
