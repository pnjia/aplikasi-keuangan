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
 * DTO respons Arus Kas (Cash Flow).
 * Memfilter pergerakan uang khusus pada akun Kas atau Bank.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowDTO {

    private Instant startDate;
    private Instant endDate;
    private BigDecimal totalCashIn;
    private BigDecimal totalCashOut;
    private BigDecimal netCashFlow;
    private List<CashFlowLineDTO> movements;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashFlowLineDTO {
        private UUID accountId;
        private String accountCode;
        private String accountName;
        private Instant transactionDate;
        private String referenceNumber;
        private String description;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}
