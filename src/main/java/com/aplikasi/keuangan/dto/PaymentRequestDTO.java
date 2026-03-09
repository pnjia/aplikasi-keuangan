package com.aplikasi.keuangan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    /**
     * Nominal pembayaran.
     */
    private BigDecimal amount;

    /**
     * ID akun tujuan Debit (Kas / Bank).
     */
    private UUID accountId;
}
