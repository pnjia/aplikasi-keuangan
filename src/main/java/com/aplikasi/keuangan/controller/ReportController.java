package com.aplikasi.keuangan.controller;

import com.aplikasi.keuangan.dto.BalanceSheetDTO;
import com.aplikasi.keuangan.dto.CashFlowDTO;
import com.aplikasi.keuangan.dto.JournalReportDTO;
import com.aplikasi.keuangan.dto.LedgerReportDTO;
import com.aplikasi.keuangan.dto.ProfitAndLossDTO;
import com.aplikasi.keuangan.dto.TrialBalanceDTO;
import com.aplikasi.keuangan.entity.CompanyRole;
import com.aplikasi.keuangan.entity.User;
import org.springframework.security.access.prepost.PreAuthorize;
import com.aplikasi.keuangan.repository.CompanyRoleRepository;
import com.aplikasi.keuangan.repository.UserRepository;
import com.aplikasi.keuangan.service.ExcelExportService;
import com.aplikasi.keuangan.service.PdfExportService;
import com.aplikasi.keuangan.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
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

    /**
     * Konversi parameter tanggal (LocalDate) menjadi Instant
     * untuk kompatibilitas dengan tipe data TIMESTAMPTZ di database.
     */
    private Instant toStartOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant toEndOfDay(LocalDate date) {
        return date.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/reports/general-journal
    // ──────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping("/general-journal")
    public ResponseEntity<?> getGeneralJournal(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "format", required = false) String format) {

        UUID companyId = getCompanyIdFromCurrentUser();
        JournalReportDTO data = reportService.getGeneralJournal(companyId, toStartOfDay(startDate), toEndOfDay(endDate));

        if ("excel".equalsIgnoreCase(format)) {
            byte[] file = excelExportService.generateGeneralJournalExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Jurnal_Umum_" + startDate + "_sd_" + endDate + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] file = pdfExportService.generateGeneralJournalPdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Jurnal_Umum_" + startDate + "_sd_" + endDate + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(file);
        }

        return ResponseEntity.ok(data);
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/reports/general-ledger/{accountId}
    // ──────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping("/general-ledger/{accountId}")
    public ResponseEntity<?> getGeneralLedger(
            @PathVariable UUID accountId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "format", required = false) String format) {

        UUID companyId = getCompanyIdFromCurrentUser();
        LedgerReportDTO data = reportService.getGeneralLedger(companyId, accountId, toStartOfDay(startDate), toEndOfDay(endDate));

        if ("excel".equalsIgnoreCase(format)) {
            byte[] file = excelExportService.generateGeneralLedgerExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Buku_Besar_" + data.getAccountCode() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] file = pdfExportService.generateGeneralLedgerPdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Buku_Besar_" + data.getAccountCode() + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(file);
        }

        return ResponseEntity.ok(data);
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/reports/trial-balance
    // ──────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping("/trial-balance")
    public ResponseEntity<?> getTrialBalance(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "format", required = false) String format) {

        UUID companyId = getCompanyIdFromCurrentUser();
        TrialBalanceDTO data = reportService.getTrialBalance(companyId, toStartOfDay(startDate), toEndOfDay(endDate));

        if ("excel".equalsIgnoreCase(format)) {
            byte[] file = excelExportService.generateTrialBalanceExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Neraca_Saldo_" + endDate + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] file = pdfExportService.generateTrialBalancePdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Neraca_Saldo_" + endDate + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(file);
        }

        return ResponseEntity.ok(data);
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/reports/profit-and-loss
    // ──────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping("/profit-and-loss")
    public ResponseEntity<?> getProfitAndLoss(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "format", required = false) String format) {

        UUID companyId = getCompanyIdFromCurrentUser();
        ProfitAndLossDTO data = reportService.getProfitAndLoss(companyId, toStartOfDay(startDate), toEndOfDay(endDate));

        if ("excel".equalsIgnoreCase(format)) {
            byte[] file = excelExportService.generateProfitAndLossExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laba_Rugi_" + startDate + "_sd_" + endDate + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] file = pdfExportService.generateProfitAndLossPdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laba_Rugi_" + startDate + "_sd_" + endDate + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(file);
        }

        return ResponseEntity.ok(data);
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/reports/balance-sheet
    // ──────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping("/balance-sheet")
    public ResponseEntity<?> getBalanceSheet(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "format", required = false) String format) {

        UUID companyId = getCompanyIdFromCurrentUser();
        BalanceSheetDTO data = reportService.getBalanceSheet(companyId, toStartOfDay(startDate), toEndOfDay(endDate));

        if ("excel".equalsIgnoreCase(format)) {
            byte[] file = excelExportService.generateBalanceSheetExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Neraca_" + endDate + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] file = pdfExportService.generateBalanceSheetPdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Neraca_" + endDate + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(file);
        }

        return ResponseEntity.ok(data);
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/reports/cash-flow
    // ──────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @GetMapping("/cash-flow")
    public ResponseEntity<?> getCashFlow(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "format", required = false) String format) {

        UUID companyId = getCompanyIdFromCurrentUser();
        CashFlowDTO data = reportService.getCashFlow(companyId, toStartOfDay(startDate), toEndOfDay(endDate));

        if ("excel".equalsIgnoreCase(format)) {
            byte[] file = excelExportService.generateCashFlowExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Arus_Kas_" + startDate + "_sd_" + endDate + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] file = pdfExportService.generateCashFlowPdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Arus_Kas_" + startDate + "_sd_" + endDate + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(file);
        }

        return ResponseEntity.ok(data);
    }
}
