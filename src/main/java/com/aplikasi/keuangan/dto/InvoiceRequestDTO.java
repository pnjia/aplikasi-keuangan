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
public class InvoiceRequestDTO {
    private UUID companyId;
    private UUID contactId;
    private String invoiceNumber;
    private Instant issueDate;
    private Instant dueDate;
    private InvoiceStatus status;
    private List<InvoiceItemDTO> items;
}
