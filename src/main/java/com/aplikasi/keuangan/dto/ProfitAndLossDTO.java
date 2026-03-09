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
 * DTO respons Laporan Laba Rugi (Profit & Loss / Income Statement).
 * Menghitung: Laba Bersih = Total Pendapatan (REVENUE) - Total Beban (EXPENSE).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitAndLossDTO {

    private Instant startDate;
    private Instant endDate;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpense;
    private BigDecimal netIncome;
    private List<ProfitAndLossLineDTO> revenueAccounts;
    private List<ProfitAndLossLineDTO> expenseAccounts;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitAndLossLineDTO {
        private UUID accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal balance;
    }
}
