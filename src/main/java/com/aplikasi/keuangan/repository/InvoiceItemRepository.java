package com.aplikasi.keuangan.repository;

import com.aplikasi.keuangan.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {
    List<InvoiceItem> findByInvoiceId(UUID invoiceId);
}
