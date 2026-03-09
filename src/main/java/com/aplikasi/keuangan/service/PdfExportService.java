package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.BalanceSheetDTO;
import com.aplikasi.keuangan.dto.CashFlowDTO;
import com.aplikasi.keuangan.dto.JournalReportDTO;
import com.aplikasi.keuangan.dto.LedgerReportDTO;
import com.aplikasi.keuangan.dto.ProfitAndLossDTO;
import com.aplikasi.keuangan.dto.TrialBalanceDTO;
import com.aplikasi.keuangan.util.CurrencyFormatUtil;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfExportService {

    private Font getTitleFont() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    }

    // 1. General Journal
    public byte[] generateGeneralJournalPdf(JournalReportDTO data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Laporan Jurnal Umum", getTitleFont());
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Tanggal");
            table.addCell("Referensi");
            table.addCell("Keterangan");
            table.addCell("Akun");
            table.addCell("Debit");
            table.addCell("Kredit");

            for (JournalReportDTO.JournalEntryDetail entry : data.getEntries()) {
                for (JournalReportDTO.JournalLineDetail line : entry.getLines()) {
                    table.addCell(entry.getTransactionDate().toString());
                    table.addCell(entry.getReferenceNumber());
                    table.addCell(entry.getDescription());
                    table.addCell(line.getAccountName());
                    table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getDebitAmount()));
                    table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getCreditAmount()));
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal men-generate file PDF Jurnal Umum", e);
        }
    }

    // 2. General Ledger
    public byte[] generateGeneralLedgerPdf(LedgerReportDTO data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Laporan Buku Besar: " + data.getAccountName(), getTitleFont());
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("Tanggal");
            table.addCell("Keterangan");
            table.addCell("Debit");
            table.addCell("Kredit");
            table.addCell("Saldo");

            for (LedgerReportDTO.LedgerLineDetail line : data.getLines()) {
                table.addCell(line.getTransactionDate() != null ? line.getTransactionDate().toString() : "-");
                table.addCell(line.getDescription() != null ? line.getDescription() : "-");
                table.addCell(line.getDebitAmount() != null ? CurrencyFormatUtil.toRupiahKompak(line.getDebitAmount()) : "Rp0");
                table.addCell(line.getCreditAmount() != null ? CurrencyFormatUtil.toRupiahKompak(line.getCreditAmount()) : "Rp0");
                table.addCell(line.getRunningBalance() != null ? CurrencyFormatUtil.toRupiahKompak(line.getRunningBalance()) : "Rp0");
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal men-generate file PDF Buku Besar", e);
        }
    }

    // 3. Trial Balance
    public byte[] generateTrialBalancePdf(TrialBalanceDTO data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Laporan Neraca Saldo", getTitleFont());
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Kode Akun");
            table.addCell("Nama Akun");
            table.addCell("Total Debit");
            table.addCell("Total Kredit");

            for (TrialBalanceDTO.TrialBalanceLineDTO line : data.getAccounts()) {
                table.addCell(line.getAccountCode());
                table.addCell(line.getAccountName());
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getTotalDebit()));
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getTotalCredit()));
            }

            // Total
            table.addCell("");
            table.addCell("GRAND TOTAL");
            table.addCell(CurrencyFormatUtil.toRupiahKompak(data.getGrandTotalDebit()));
            table.addCell(CurrencyFormatUtil.toRupiahKompak(data.getGrandTotalCredit()));

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal men-generate file PDF Neraca Saldo", e);
        }
    }

    // 4. Profit And Loss
    public byte[] generateProfitAndLossPdf(ProfitAndLossDTO data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Laporan Laba Rugi", getTitleFont());
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell("Keterangan");
            table.addCell("Nilai");

            table.addCell("--- PENDAPATAN ---");
            table.addCell("");
            for (ProfitAndLossDTO.ProfitAndLossLineDTO line : data.getRevenueAccounts()) {
                table.addCell(line.getAccountName());
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getBalance()));
            }

            table.addCell("--- BEBAN ---");
            table.addCell("");
            for (ProfitAndLossDTO.ProfitAndLossLineDTO line : data.getExpenseAccounts()) {
                table.addCell(line.getAccountName());
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getBalance()));
            }

            table.addCell("LABA BERSIH (NET INCOME)");
            table.addCell(CurrencyFormatUtil.toRupiahKompak(data.getNetIncome()));

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal men-generate file PDF Laba Rugi", e);
        }
    }

    // 5. Balance Sheet
    public byte[] generateBalanceSheetPdf(BalanceSheetDTO data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Laporan Neraca", getTitleFont());
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Kategori");
            table.addCell("Nama Akun");
            table.addCell("Nilai");

            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getAssetAccounts()) {
                table.addCell("ASET");
                table.addCell(line.getAccountName());
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getBalance()));
            }
            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getLiabilityAccounts()) {
                table.addCell("LIABILITAS");
                table.addCell(line.getAccountName());
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getBalance()));
            }
            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getEquityAccounts()) {
                table.addCell("EKUITAS");
                table.addCell(line.getAccountName());
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getBalance()));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal men-generate file PDF Neraca", e);
        }
    }

    // 6. Cash Flow
    public byte[] generateCashFlowPdf(CashFlowDTO data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Laporan Arus Kas", getTitleFont());
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Tanggal");
            table.addCell("Keterangan");
            table.addCell("Masuk (Debit)");
            table.addCell("Keluar (Kredit)");

            for (CashFlowDTO.CashFlowLineDTO line : data.getMovements()) {
                table.addCell(line.getTransactionDate().toString());
                table.addCell(line.getDescription());
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getDebitAmount()));
                table.addCell(CurrencyFormatUtil.toRupiahKompak(line.getCreditAmount()));
            }

            table.addCell("");
            table.addCell("NET CASH FLOW");
            table.addCell("");
            table.addCell(CurrencyFormatUtil.toRupiahKompak(data.getNetCashFlow()));

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal men-generate file PDF Arus Kas", e);
        }
    }
}
