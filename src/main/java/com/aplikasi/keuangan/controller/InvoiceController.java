package com.aplikasi.keuangan.controller;

import com.aplikasi.keuangan.dto.InvoiceRequestDTO;
import com.aplikasi.keuangan.dto.InvoiceResponseDTO;
import com.aplikasi.keuangan.dto.PaymentRequestDTO;
import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.User;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import com.aplikasi.keuangan.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;
    private final CompanyRoleRepository companyRoleRepository;

    // ──────────────────────────────────────────────
    // Helper: Ambil companyId dari JWT context user yang login
    // ──────────────────────────────────────────────

    private UUID getCompanyIdFromCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CompanyRole> roles = companyRoleRepository.findByUserId(user.getId());
        if (roles.isEmpty()) {
            throw new RuntimeException("User does not belong to any company");
        }
        return roles.get(0).getCompanyId();
    }

    // ──────────────────────────────────────────────
    // POST /api/v1/invoices — Membuat Tagihan Baru
    // ──────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO request) {
        // Set companyId dari context JWT, bukan dari input client
        request.setCompanyId(getCompanyIdFromCurrentUser());
        InvoiceResponseDTO response = invoiceService.createInvoice(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/invoices — Daftar Tagihan (Paginasi)
    // ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<InvoiceResponseDTO>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID companyId = getCompanyIdFromCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(invoiceService.getInvoicesByCompanyId(companyId, pageable));
    }

    // ──────────────────────────────────────────────
    // PUT /api/v1/invoices/{id}/pay — Proses Pembayaran
    // ──────────────────────────────────────────────

    @PutMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponseDTO> payInvoice(
            @PathVariable UUID id,
            @RequestBody PaymentRequestDTO request) {
        InvoiceResponseDTO response = invoiceService.payInvoice(id, request);
        return ResponseEntity.ok(response);
    }
}
