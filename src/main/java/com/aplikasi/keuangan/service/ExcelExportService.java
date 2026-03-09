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

    // 5. Balance Sheet
    public byte[] generateBalanceSheetExcel(BalanceSheetDTO data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Neraca");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Kategori");
            headerRow.createCell(1).setCellValue("Kode Akun");
            headerRow.createCell(2).setCellValue("Nama Akun");
            headerRow.createCell(3).setCellValue("Nilai");

            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowIdx = 1;
            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getAssetAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue("ASET");
                row.createCell(1).setCellValue(line.getAccountCode());
                row.createCell(2).setCellValue(line.getAccountName());
                setCurrencyCell(row, 3, line.getBalance(), currencyStyle);
            }

            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getLiabilityAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue("LIABILITAS");
                row.createCell(1).setCellValue(line.getAccountCode());
                row.createCell(2).setCellValue(line.getAccountName());
                setCurrencyCell(row, 3, line.getBalance(), currencyStyle);
            }

            for (BalanceSheetDTO.BalanceSheetLineDTO line : data.getEquityAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue("EKUITAS");
                row.createCell(1).setCellValue(line.getAccountCode());
                row.createCell(2).setCellValue(line.getAccountName());
                setCurrencyCell(row, 3, line.getBalance(), currencyStyle);
            }

            Row totalRow = sheet.createRow(rowIdx++);
            totalRow.createCell(2).setCellValue("TOTAL ASET");
            setCurrencyCell(totalRow, 3, data.getTotalAsset(), currencyStyle);

            Row totalRow2 = sheet.createRow(rowIdx++);
            totalRow2.createCell(2).setCellValue("TOTAL LIABILITAS + EKUITAS");
            setCurrencyCell(totalRow2, 3, data.getTotalLiability().add(data.getTotalEquity()), currencyStyle);

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
