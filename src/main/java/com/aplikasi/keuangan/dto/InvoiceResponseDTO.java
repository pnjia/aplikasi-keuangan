package com.aplikasi.keuangan.dto;

import com.aplikasi.keuangan.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDTO {
    private UUID id;
    private UUID companyId;
    private UUID contactId;
    private String invoiceNumber;
    private Instant issueDate;
    private Instant dueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private List<InvoiceItemResponseDTO> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemResponseDTO {
        private UUID id;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
