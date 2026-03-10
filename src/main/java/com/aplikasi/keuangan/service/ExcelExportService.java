package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.BalanceSheetDTO;
import com.aplikasi.keuangan.dto.CashFlowDTO;
import com.aplikasi.keuangan.dto.JournalReportDTO;
import com.aplikasi.keuangan.dto.LedgerReportDTO;
import com.aplikasi.keuangan.dto.ProfitAndLossDTO;
import com.aplikasi.keuangan.dto.TrialBalanceDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelExportService {

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        // Menggunakan format custom akuntansi Excel "Rp10.000" tanpa angka desimal di belakang
        style.setDataFormat(format.getFormat("\"Rp\"#,##0;[Red]\\-\"Rp\"#,##0"));
        return style;
    }

    private void setCurrencyCell(Row row, int column, java.math.BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue(0);
        }
        cell.setCellStyle(style);
    }

    // 1. General Journal
    public byte[] generateGeneralJournalExcel(JournalReportDTO data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Jurnal Umum");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Tanggal");
            headerRow.createCell(1).setCellValue("Referensi");
            headerRow.createCell(2).setCellValue("Keterangan");
            headerRow.createCell(3).setCellValue("Kode Akun");
            headerRow.createCell(4).setCellValue("Nama Akun");
            headerRow.createCell(5).setCellValue("Debit");
            headerRow.createCell(6).setCellValue("Kredit");

            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowIdx = 1;
            for (JournalReportDTO.JournalEntryDetail entry : data.getEntries()) {
                for (JournalReportDTO.JournalLineDetail line : entry.getLines()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(entry.getTransactionDate().toString());
                    row.createCell(1).setCellValue(entry.getReferenceNumber());
                    row.createCell(2).setCellValue(entry.getDescription());
                    row.createCell(3).setCellValue(line.getAccountCode());
                    row.createCell(4).setCellValue(line.getAccountName());
                    setCurrencyCell(row, 5, line.getDebitAmount(), currencyStyle);
                    setCurrencyCell(row, 6, line.getCreditAmount(), currencyStyle);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gagal men-generate file Excel Jurnal Umum", e);
        }
    }

    // 2. General Ledger
    public byte[] generateGeneralLedgerExcel(LedgerReportDTO data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Buku Besar");

            Row infoRow = sheet.createRow(0);
            infoRow.createCell(0).setCellValue("Akun: " + data.getAccountName() + " (" + data.getAccountCode() + ")");

            Row headerRow = sheet.createRow(1);
            headerRow.createCell(0).setCellValue("Tanggal");
            headerRow.createCell(1).setCellValue("Referensi");
            headerRow.createCell(2).setCellValue("Keterangan");
            headerRow.createCell(3).setCellValue("Debit");
            headerRow.createCell(4).setCellValue("Kredit");
            headerRow.createCell(5).setCellValue("Saldo Berjalan");

            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowIdx = 2;
            for (LedgerReportDTO.LedgerLineDetail line : data.getLines()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(line.getTransactionDate() != null ? line.getTransactionDate().toString() : "-");
                row.createCell(1).setCellValue(line.getReferenceNumber() != null ? line.getReferenceNumber() : "-");
                row.createCell(2).setCellValue(line.getDescription() != null ? line.getDescription() : "-");
                setCurrencyCell(row, 3, line.getDebitAmount(), currencyStyle);
                setCurrencyCell(row, 4, line.getCreditAmount(), currencyStyle);
                setCurrencyCell(row, 5, line.getRunningBalance(), currencyStyle);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gagal men-generate file Excel Buku Besar", e);
        }
    }

    // 3. Trial Balance
    public byte[] generateTrialBalanceExcel(TrialBalanceDTO data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Neraca Saldo");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Kode Akun");
            headerRow.createCell(1).setCellValue("Nama Akun");
            headerRow.createCell(2).setCellValue("Total Debit");
            headerRow.createCell(3).setCellValue("Total Kredit");

            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowIdx = 1;
            for (TrialBalanceDTO.TrialBalanceLineDTO line : data.getAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(line.getAccountCode());
                row.createCell(1).setCellValue(line.getAccountName());
                setCurrencyCell(row, 2, line.getTotalDebit(), currencyStyle);
                setCurrencyCell(row, 3, line.getTotalCredit(), currencyStyle);
            }

            // Total
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(1).setCellValue("GRAND TOTAL");
            setCurrencyCell(totalRow, 2, data.getGrandTotalDebit(), currencyStyle);
            setCurrencyCell(totalRow, 3, data.getGrandTotalCredit(), currencyStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gagal men-generate file Excel Neraca Saldo", e);
        }
    }

    // 4. Profit And Loss
    public byte[] generateProfitAndLossExcel(ProfitAndLossDTO data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Laba Rugi");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Kode Akun");
            headerRow.createCell(1).setCellValue("Nama Akun");
            headerRow.createCell(2).setCellValue("Nilai");

            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowIdx = 1;

            Row revTitle = sheet.createRow(rowIdx++);
            revTitle.createCell(0).setCellValue("--- PENDAPATAN ---");

            for (ProfitAndLossDTO.ProfitAndLossLineDTO line : data.getRevenueAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(line.getAccountCode());
                row.createCell(1).setCellValue(line.getAccountName());
                setCurrencyCell(row, 2, line.getBalance(), currencyStyle);
            }

            Row expTitle = sheet.createRow(rowIdx++);
            expTitle.createCell(0).setCellValue("--- BEBAN ---");

            for (ProfitAndLossDTO.ProfitAndLossLineDTO line : data.getExpenseAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(line.getAccountCode());
                row.createCell(1).setCellValue(line.getAccountName());
                setCurrencyCell(row, 2, line.getBalance(), currencyStyle);
            }

            Row totalRow = sheet.createRow(rowIdx++);
            totalRow.createCell(1).setCellValue("LABA BERSIH (NET INCOME)");
            setCurrencyCell(totalRow, 2, data.getNetIncome(), currencyStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gagal men-generate file Excel Laba Rugi", e);
        }
    }

    // 5. Balance Sheet (Format Berblok Standar Akuntansi)
    public byte[] generateBalanceSheetExcel(BalanceSheetDTO data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Neraca");

            // Style untuk header blok (bold + background)
            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Style untuk subtotal (bold only)
            CellStyle subtotalStyle = workbook.createCellStyle();
            subtotalStyle.setFont(boldFont);

            // Style untuk subtotal currency (bold + format Rp)
            CellStyle subtotalCurrencyStyle = workbook.createCellStyle();
            subtotalCurrencyStyle.setFont(boldFont);
            DataFormat fmt = workbook.createDataFormat();
            subtotalCurrencyStyle.setDataFormat(fmt.getFormat("\"Rp\"#,##0;[Red]\\-\"Rp\"#,##0"));

            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowIdx = 0;

            // Judul
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Laporan Neraca (Balance Sheet)");
            titleCell.setCellStyle(subtotalStyle);
            rowIdx++; // baris kosong

            // ── BLOK ASET ──
            Row assetHeader = sheet.createRow(rowIdx++);
            Cell assetHeaderCell = assetHeader.createCell(0);
            assetHeaderCell.setCellValue("ASET");
            assetHeaderCell.setCellStyle(headerStyle);

            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getAssetAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue("   " + line.getAccountName());
                setCurrencyCell(row, 1, line.getBalance(), currencyStyle);
            }

            Row subtotalAsset = sheet.createRow(rowIdx++);
            Cell lblAsset = subtotalAsset.createCell(0);
            lblAsset.setCellValue("Total Aset");
            lblAsset.setCellStyle(subtotalStyle);
            setCurrencyCell(subtotalAsset, 1, data.getTotalAssets(), subtotalCurrencyStyle);

            rowIdx++; // baris kosong

            // ── BLOK KEWAJIBAN ──
            Row liabHeader = sheet.createRow(rowIdx++);
            Cell liabHeaderCell = liabHeader.createCell(0);
            liabHeaderCell.setCellValue("KEWAJIBAN");
            liabHeaderCell.setCellStyle(headerStyle);

            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getLiabilityAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue("   " + line.getAccountName());
                setCurrencyCell(row, 1, line.getBalance(), currencyStyle);
            }

            Row subtotalLiab = sheet.createRow(rowIdx++);
            Cell lblLiab = subtotalLiab.createCell(0);
            lblLiab.setCellValue("Total Kewajiban");
            lblLiab.setCellStyle(subtotalStyle);
            setCurrencyCell(subtotalLiab, 1, data.getTotalLiabilities(), subtotalCurrencyStyle);

            rowIdx++; // baris kosong

            // ── BLOK EKUITAS ──
            Row eqHeader = sheet.createRow(rowIdx++);
            Cell eqHeaderCell = eqHeader.createCell(0);
            eqHeaderCell.setCellValue("EKUITAS");
            eqHeaderCell.setCellStyle(headerStyle);

            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getEquityAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue("   " + line.getAccountName());
                setCurrencyCell(row, 1, line.getBalance(), currencyStyle);
            }

            Row subtotalEq = sheet.createRow(rowIdx++);
            Cell lblEq = subtotalEq.createCell(0);
            lblEq.setCellValue("Total Ekuitas");
            lblEq.setCellStyle(subtotalStyle);
            setCurrencyCell(subtotalEq, 1, data.getTotalEquities(), subtotalCurrencyStyle);

            rowIdx++; // baris kosong

            // ── GRAND TOTAL: KEWAJIBAN + EKUITAS ──
            Row grandRow = sheet.createRow(rowIdx++);
            Cell grandLabel = grandRow.createCell(0);
            grandLabel.setCellValue("TOTAL KEWAJIBAN & EKUITAS");
            grandLabel.setCellStyle(subtotalStyle);
            setCurrencyCell(grandRow, 1, data.getTotalLiabilitiesAndEquities(), subtotalCurrencyStyle);

            // Auto-size kolom
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gagal men-generate file Excel Neraca", e);
        }
    }

    // 6. Cash Flow
    public byte[] generateCashFlowExcel(CashFlowDTO data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Arus Kas");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Tanggal");
            headerRow.createCell(1).setCellValue("Referensi");
            headerRow.createCell(2).setCellValue("Keterangan");
            headerRow.createCell(3).setCellValue("Kas Masuk (Debit)");
            headerRow.createCell(4).setCellValue("Kas Keluar (Kredit)");

            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowIdx = 1;
            for (CashFlowDTO.CashFlowLineDTO line : data.getMovements()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(line.getTransactionDate().toString());
                row.createCell(1).setCellValue(line.getReferenceNumber());
                row.createCell(2).setCellValue(line.getDescription());
                setCurrencyCell(row, 3, line.getDebitAmount(), currencyStyle);
                setCurrencyCell(row, 4, line.getCreditAmount(), currencyStyle);
            }

            Row totalRow = sheet.createRow(rowIdx++);
            totalRow.createCell(2).setCellValue("NET CASH FLOW");
            setCurrencyCell(totalRow, 3, data.getNetCashFlow(), currencyStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gagal men-generate file Excel Arus Kas", e);
        }
    }
}
