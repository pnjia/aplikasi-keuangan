package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.InvoiceItemDTO;
import com.aplikasi.keuangan.dto.InvoiceRequestDTO;
import com.aplikasi.keuangan.dto.InvoiceResponseDTO;
import com.aplikasi.keuangan.dto.PaymentRequestDTO;
import com.aplikasi.keuangan.entity.Contact;
import com.aplikasi.keuangan.entity.ContactType;
import com.aplikasi.keuangan.entity.Invoice;
import com.aplikasi.keuangan.entity.InvoiceItem;
import com.aplikasi.keuangan.entity.InvoiceStatus;
import com.aplikasi.keuangan.entity.JournalEntry;
import com.aplikasi.keuangan.entity.JournalLine;
import com.aplikasi.keuangan.repository.AccountRepository;
import com.aplikasi.keuangan.repository.ContactRepository;
import com.aplikasi.keuangan.repository.InvoiceRepository;
import com.aplikasi.keuangan.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;

    // ──────────────────────────────────────────────
    // 1. Membuat Tagihan Baru (Create Invoice)
    // ──────────────────────────────────────────────

    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO request) {

        Invoice invoice = Invoice.builder()
                .companyId(request.getCompanyId())
                .contactId(request.getContactId())
                .invoiceNumber(request.getInvoiceNumber())
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .status(request.getStatus() != null ? request.getStatus() : InvoiceStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO) // akan dihitung ulang di bawah
                .build();

        // Hitung subtotal setiap item dari quantity × unitPrice di sisi server
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (InvoiceItemDTO itemDto : request.getItems()) {
                BigDecimal subtotal = itemDto.getQuantity().multiply(itemDto.getUnitPrice());

                InvoiceItem item = InvoiceItem.builder()
                        .description(itemDto.getDescription())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .subtotal(subtotal)
                        .build();

                invoice.addItem(item);
                calculatedTotal = calculatedTotal.add(subtotal);
            }
        }

        // Set total amount hasil kalkulasi server-side
        invoice.setTotalAmount(calculatedTotal);

        Invoice saved = invoiceRepository.save(invoice);
        return mapToResponse(saved);
    }

    // ──────────────────────────────────────────────
    // 2. Mengambil Daftar Tagihan (List Invoices)
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<InvoiceResponseDTO> getInvoicesByCompanyId(UUID companyId, Pageable pageable) {
        return invoiceRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponseDTO> getInvoicesByCompanyIdAndContactId(UUID companyId, UUID contactId, Pageable pageable) {
        return invoiceRepository.findByCompanyIdAndContactIdAndDeletedAtIsNull(companyId, contactId, pageable)
                .map(this::mapToResponse);
    }

    // ──────────────────────────────────────────────
    // 3. ATURAN MUTLAK: Proses Pembayaran Tagihan
    //    dengan Jurnal Ganda (Double Entry)
    // ──────────────────────────────────────────────

    @Transactional
    public InvoiceResponseDTO payInvoice(UUID invoiceId, PaymentRequestDTO request) {

        // --- Langkah 1: Validasi keberadaan dan status invoice ---
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice tidak ditemukan dengan ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice sudah berstatus PAID. Tidak dapat memproses pembayaran ulang.");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new RuntimeException("Invoice berstatus CANCELLED. Tidak dapat memproses pembayaran.");
        }

        // --- Langkah 2: Validasi keberadaan akun tujuan (Kas/Bank) ---
        accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun tujuan tidak ditemukan dengan ID: " + request.getAccountId()));

        // --- Langkah 3: Ubah status Invoice menjadi PAID ---
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        // --- Langkah 4: Buat JournalEntry sebagai referensi pembayaran ---
        JournalEntry journalEntry = JournalEntry.builder()
                .companyId(invoice.getCompanyId())
                .transactionDate(Instant.now())
                .referenceNumber(invoice.getInvoiceNumber())
                .description("Pembayaran tagihan: " + invoice.getInvoiceNumber())
                .build();

        // --- Langkah 4.5: Menentukan Tipe Jurnal berdasarkan Tipe Kontak ---
        Contact contact = contactRepository.findById(invoice.getContactId())
                .orElseThrow(() -> new RuntimeException("Kontak tidak ditemukan"));

        JournalLine debitLine;
        JournalLine creditLine;
        BigDecimal paymentAmount = invoice.getTotalAmount();

        if (contact.getType() == ContactType.CUSTOMER) {
            // CUSTOMER (Pelanggan) -> Invoice Penjualan
            // Cash-basis: Kas/Bank Bertambah (DEBIT), Pendapatan Jasa Bertambah (KREDIT)
            UUID pendapatanAccountId = accountRepository
                    .findByCompanyIdAndAccountCode(invoice.getCompanyId(), "4-1001")
                    .orElseThrow(() -> new RuntimeException("Akun Pendapatan Jasa (4-1001) tidak ditemukan."))
                    .getId();

            debitLine = JournalLine.builder()
                    .accountId(request.getAccountId()) // Kas/Bank
                    .debitAmount(paymentAmount)
                    .creditAmount(BigDecimal.ZERO)
                    .build();

            creditLine = JournalLine.builder()
                    .accountId(pendapatanAccountId)    // Pendapatan Jasa
                    .debitAmount(BigDecimal.ZERO)
                    .creditAmount(paymentAmount)
                    .build();

        } else {
            // VENDOR (Pemasok) -> Invoice Pembelian/Pengeluaran
            // Cash-basis: Beban Operasional Bertambah (DEBIT), Kas/Bank Berkurang (KREDIT)
            UUID bebanAccountId = accountRepository
                    .findByCompanyIdAndAccountCode(invoice.getCompanyId(), "5-1001")
                    .orElseThrow(() -> new RuntimeException("Akun Beban Operasional (5-1001) tidak ditemukan."))
                    .getId();

            debitLine = JournalLine.builder()
                    .accountId(bebanAccountId)         // Beban Operasional
                    .debitAmount(paymentAmount)
                    .creditAmount(BigDecimal.ZERO)
                    .build();

            creditLine = JournalLine.builder()
                    .accountId(request.getAccountId()) // Kas/Bank
                    .debitAmount(BigDecimal.ZERO)
                    .creditAmount(paymentAmount)
                    .build();
        }

        // --- Langkah 6: Validasi keseimbangan Debit == Kredit ---
        if (debitLine.getDebitAmount().compareTo(creditLine.getCreditAmount()) != 0) {
            throw new RuntimeException("FATAL: Total Debit tidak sama dengan total Kredit. Transaksi dibatalkan.");
        }

        // Tambahkan baris jurnal ke entri menggunakan helper method bidireksional
        journalEntry.addLine(debitLine);
        journalEntry.addLine(creditLine);

        // --- Langkah 7: Simpan JournalEntry + JournalLines (cascade) ---
        journalEntryRepository.save(journalEntry);

        return mapToResponse(invoice);
    }

    // ──────────────────────────────────────────────
    // Mapper: Entity → DTO (Isolasi Entitas)
    // ──────────────────────────────────────────────

    private InvoiceResponseDTO mapToResponse(Invoice invoice) {
        List<InvoiceResponseDTO.InvoiceItemResponseDTO> itemDtos = null;

        if (invoice.getItems() != null) {
            itemDtos = invoice.getItems().stream()
                    .map(item -> InvoiceResponseDTO.InvoiceItemResponseDTO.builder()
                            .id(item.getId())
                            .description(item.getDescription())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(item.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
        }

        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .contactId(invoice.getContactId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .items(itemDtos)
                .build();
    }
}
