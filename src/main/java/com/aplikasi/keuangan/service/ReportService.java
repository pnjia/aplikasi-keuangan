package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.BalanceSheetDTO;
import com.aplikasi.keuangan.dto.CashFlowDTO;
import com.aplikasi.keuangan.dto.JournalReportDTO;
import com.aplikasi.keuangan.dto.LedgerReportDTO;
import com.aplikasi.keuangan.dto.ProfitAndLossDTO;
import com.aplikasi.keuangan.dto.TrialBalanceDTO;
import com.aplikasi.keuangan.entity.Account;
import com.aplikasi.keuangan.entity.JournalEntry;
import com.aplikasi.keuangan.repository.AccountRepository;
import com.aplikasi.keuangan.repository.JournalEntryRepository;
import com.aplikasi.keuangan.repository.JournalLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;

    // ══════════════════════════════════════════════
    // 1. JURNAL UMUM (General Journal)
    // ══════════════════════════════════════════════

    @Transactional(readOnly = true)
    public JournalReportDTO getGeneralJournal(UUID companyId, Instant startDate, Instant endDate) {

        List<JournalEntry> entries = journalEntryRepository
                .findByCompanyIdAndTransactionDateBetweenAndDeletedAtIsNull(companyId, startDate, endDate);

        List<JournalReportDTO.JournalEntryDetail> entryDetails = entries.stream()
                .map(entry -> {
                    List<JournalReportDTO.JournalLineDetail> lineDetails = entry.getLines().stream()
                            .map(line -> {
                                Account account = accountRepository.findById(line.getAccountId()).orElse(null);
                                return JournalReportDTO.JournalLineDetail.builder()
                                        .accountId(line.getAccountId())
                                        .accountCode(account != null ? account.getAccountCode() : "-")
                                        .accountName(account != null ? account.getAccountName() : "-")
                                        .debitAmount(line.getDebitAmount())
                                        .creditAmount(line.getCreditAmount())
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return JournalReportDTO.JournalEntryDetail.builder()
                            .journalEntryId(entry.getId())
                            .transactionDate(entry.getTransactionDate())
                            .referenceNumber(entry.getReferenceNumber())
                            .description(entry.getDescription())
                            .lines(lineDetails)
                            .build();
                })
                .collect(Collectors.toList());

        return JournalReportDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .entries(entryDetails)
                .build();
    }

    // ══════════════════════════════════════════════
    // 2. BUKU BESAR (General Ledger) — Satu Akun
    // ══════════════════════════════════════════════

    @Transactional(readOnly = true)
    public LedgerReportDTO getGeneralLedger(UUID companyId, UUID accountId, Instant startDate, Instant endDate) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Akun tidak ditemukan dengan ID: " + accountId));

        List<Object[]> rawLines = journalLineRepository
                .findLedgerDetailsByAccount(companyId, accountId, startDate, endDate);

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal runningBalance = BigDecimal.ZERO;

        List<LedgerReportDTO.LedgerLineDetail> lineDetails = new ArrayList<>();

        for (Object[] row : rawLines) {
            BigDecimal debit = toBigDecimal(row[3]);
            BigDecimal credit = toBigDecimal(row[4]);

            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
            runningBalance = runningBalance.add(debit).subtract(credit);

            lineDetails.add(LedgerReportDTO.LedgerLineDetail.builder()
                    .transactionDate(toInstant(row[0]))
                    .referenceNumber(toStringValue(row[1]))
                    .description(toStringValue(row[2]))
                    .debitAmount(debit)
                    .creditAmount(credit)
                    .runningBalance(runningBalance)
                    .build());
        }

        return LedgerReportDTO.builder()
                .accountId(accountId)
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType().name())
                .startDate(startDate)
                .endDate(endDate)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .balance(runningBalance)
                .lines(lineDetails)
                .build();
    }

    // ══════════════════════════════════════════════
    // 3. NERACA SALDO (Trial Balance)
    // ══════════════════════════════════════════════

    @Transactional(readOnly = true)
    public TrialBalanceDTO getTrialBalance(UUID companyId, Instant startDate, Instant endDate) {

        List<Object[]> rawData = journalLineRepository
                .aggregateByAccountAndType(companyId, startDate, endDate);

        BigDecimal grandTotalDebit = BigDecimal.ZERO;
        BigDecimal grandTotalCredit = BigDecimal.ZERO;

        List<TrialBalanceDTO.TrialBalanceLineDTO> lines = new ArrayList<>();

        for (Object[] row : rawData) {
            String accountType = toStringValue(row[3]);
            BigDecimal rawDebit = toBigDecimal(row[4]);
            BigDecimal rawCredit = toBigDecimal(row[5]);

            // Hitung selisih mutasi untuk mendapat Saldo Akhir Bersih (Net Balance)
            BigDecimal netDebit = BigDecimal.ZERO;
            BigDecimal netCredit = BigDecimal.ZERO;

            if ("ASSET".equals(accountType) || "EXPENSE".equals(accountType)) {
                // Saldo Normal: DEBIT → net = rawDebit - rawCredit
                BigDecimal net = rawDebit.subtract(rawCredit);
                if (net.compareTo(BigDecimal.ZERO) >= 0) {
                    netDebit = net;   // Saldo positif → tampilkan di Debit
                } else {
                    netCredit = net.abs(); // Saldo terbalik → tampilkan di Kredit
                }
            } else if ("LIABILITY".equals(accountType) || "EQUITY".equals(accountType)
                    || "REVENUE".equals(accountType)) {
                // Saldo Normal: KREDIT → net = rawCredit - rawDebit
                BigDecimal net = rawCredit.subtract(rawDebit);
                if (net.compareTo(BigDecimal.ZERO) >= 0) {
                    netCredit = net;  // Saldo positif → tampilkan di Kredit
                } else {
                    netDebit = net.abs(); // Saldo terbalik → tampilkan di Debit
                }
            } else {
                // Fallback untuk tipe akun yang belum dikenal
                netDebit = rawDebit;
                netCredit = rawCredit;
            }

            grandTotalDebit = grandTotalDebit.add(netDebit);
            grandTotalCredit = grandTotalCredit.add(netCredit);

            lines.add(TrialBalanceDTO.TrialBalanceLineDTO.builder()
                    .accountId(toUUID(row[0]))
                    .accountCode(toStringValue(row[1]))
                    .accountName(toStringValue(row[2]))
                    .accountType(accountType)
                    .totalDebit(netDebit)
                    .totalCredit(netCredit)
                    .build());
        }

        return TrialBalanceDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .grandTotalDebit(grandTotalDebit)
                .grandTotalCredit(grandTotalCredit)
                .accounts(lines)
                .build();
    }

    // ══════════════════════════════════════════════
    // 4. LABA RUGI (Profit & Loss)
    //    Rumus: Laba Bersih = REVENUE - EXPENSE
    // ══════════════════════════════════════════════

    @Transactional(readOnly = true)
    public ProfitAndLossDTO getProfitAndLoss(UUID companyId, Instant startDate, Instant endDate) {

        List<Object[]> rawData = journalLineRepository
                .aggregateByAccountAndType(companyId, startDate, endDate);

        List<ProfitAndLossDTO.ProfitAndLossLineDTO> revenueAccounts = new ArrayList<>();
        List<ProfitAndLossDTO.ProfitAndLossLineDTO> expenseAccounts = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Object[] row : rawData) {
            String accountType = toStringValue(row[3]);
            BigDecimal debit = toBigDecimal(row[4]);
            BigDecimal credit = toBigDecimal(row[5]);

            if ("REVENUE".equals(accountType)) {
                // Saldo Pendapatan = Kredit - Debit (normal saldo kredit)
                BigDecimal balance = credit.subtract(debit);
                totalRevenue = totalRevenue.add(balance);

                revenueAccounts.add(ProfitAndLossDTO.ProfitAndLossLineDTO.builder()
                        .accountId(toUUID(row[0]))
                        .accountCode(toStringValue(row[1]))
                        .accountName(toStringValue(row[2]))
                        .balance(balance)
                        .build());

            } else if ("EXPENSE".equals(accountType)) {
                // Saldo Beban = Debit - Kredit (normal saldo debit)
                BigDecimal balance = debit.subtract(credit);
                totalExpense = totalExpense.add(balance);

                expenseAccounts.add(ProfitAndLossDTO.ProfitAndLossLineDTO.builder()
                        .accountId(toUUID(row[0]))
                        .accountCode(toStringValue(row[1]))
                        .accountName(toStringValue(row[2]))
                        .balance(balance)
                        .build());
            }
        }

        return ProfitAndLossDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalExpense(totalExpense)
                .netIncome(totalRevenue.subtract(totalExpense))
                .revenueAccounts(revenueAccounts)
                .expenseAccounts(expenseAccounts)
                .build();
    }

    // ══════════════════════════════════════════════
    // 5. NERACA (Balance Sheet)
    //    Rumus: Aset = Liabilitas + Ekuitas
    // ══════════════════════════════════════════════

    @Transactional(readOnly = true)
    public BalanceSheetDTO getBalanceSheet(UUID companyId, Instant startDate, Instant endDate) {

        List<Object[]> rawData = journalLineRepository
                .aggregateByAccountAndType(companyId, startDate, endDate);

        List<BalanceSheetDTO.BalanceSheetLineDTO> assetAccounts = new ArrayList<>();
        List<BalanceSheetDTO.BalanceSheetLineDTO> liabilityAccounts = new ArrayList<>();
        List<BalanceSheetDTO.BalanceSheetLineDTO> equityAccounts = new ArrayList<>();

        BigDecimal totalAsset = BigDecimal.ZERO;
        BigDecimal totalLiability = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (Object[] row : rawData) {
            String accountType = toStringValue(row[3]);
            BigDecimal debit = toBigDecimal(row[4]);
            BigDecimal credit = toBigDecimal(row[5]);

            switch (accountType) {
                case "ASSET" -> {
                    // Saldo Aset = Debit - Kredit (normal saldo debit)
                    BigDecimal balance = debit.subtract(credit);
                    totalAsset = totalAsset.add(balance);
                    assetAccounts.add(buildBalanceSheetLine(row, balance));
                }
                case "LIABILITY" -> {
                    // Saldo Liabilitas = Kredit - Debit (normal saldo kredit)
                    BigDecimal balance = credit.subtract(debit);
                    totalLiability = totalLiability.add(balance);
                    liabilityAccounts.add(buildBalanceSheetLine(row, balance));
                }
                case "EQUITY" -> {
                    // Saldo Ekuitas = Kredit - Debit (normal saldo kredit)
                    BigDecimal balance = credit.subtract(debit);
                    totalEquity = totalEquity.add(balance);
                    equityAccounts.add(buildBalanceSheetLine(row, balance));
                }
                default -> {
                    // REVENUE dan EXPENSE tidak ditampilkan di Neraca
                }
            }
        }

        return BalanceSheetDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalAsset(totalAsset)
                .totalLiability(totalLiability)
                .totalEquity(totalEquity)
                .assetAccounts(assetAccounts)
                .liabilityAccounts(liabilityAccounts)
                .equityAccounts(equityAccounts)
                .build();
    }

    // ══════════════════════════════════════════════
    // 6. ARUS KAS (Cash Flow)
    //    Filter: hanya akun Kas atau Bank
    // ══════════════════════════════════════════════

    @Transactional(readOnly = true)
    public CashFlowDTO getCashFlow(UUID companyId, Instant startDate, Instant endDate) {

        List<Object[]> rawData = journalLineRepository
                .findCashFlowMovements(companyId, startDate, endDate);

        BigDecimal totalCashIn = BigDecimal.ZERO;
        BigDecimal totalCashOut = BigDecimal.ZERO;

        List<CashFlowDTO.CashFlowLineDTO> movements = new ArrayList<>();

        for (Object[] row : rawData) {
            BigDecimal debit = toBigDecimal(row[6]);
            BigDecimal credit = toBigDecimal(row[7]);

            totalCashIn = totalCashIn.add(debit);
            totalCashOut = totalCashOut.add(credit);

            movements.add(CashFlowDTO.CashFlowLineDTO.builder()
                    .accountId(toUUID(row[0]))
                    .accountCode(toStringValue(row[1]))
                    .accountName(toStringValue(row[2]))
                    .transactionDate(toInstant(row[3]))
                    .referenceNumber(toStringValue(row[4]))
                    .description(toStringValue(row[5]))
                    .debitAmount(debit)
                    .creditAmount(credit)
                    .build());
        }

        return CashFlowDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalCashIn(totalCashIn)
                .totalCashOut(totalCashOut)
                .netCashFlow(totalCashIn.subtract(totalCashOut))
                .movements(movements)
                .build();
    }

    // ══════════════════════════════════════════════
    // Helper Methods: Konversi tipe data dari Native Query
    // ══════════════════════════════════════════════

    private BalanceSheetDTO.BalanceSheetLineDTO buildBalanceSheetLine(Object[] row, BigDecimal balance) {
        return BalanceSheetDTO.BalanceSheetLineDTO.builder()
                .accountId(toUUID(row[0]))
                .accountCode(toStringValue(row[1]))
                .accountName(toStringValue(row[2]))
                .balance(balance)
                .build();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return new BigDecimal(value.toString());
    }

    private UUID toUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        return UUID.fromString(value.toString());
    }

    private String toStringValue(Object value) {
        return value != null ? value.toString() : "-";
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Instant) return (Instant) value;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toInstant();
        if (value instanceof java.time.OffsetDateTime) return ((java.time.OffsetDateTime) value).toInstant();
        return Instant.parse(value.toString());
    }
}
