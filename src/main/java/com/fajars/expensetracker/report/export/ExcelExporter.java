package com.fajars.expensetracker.report.export;

import com.fajars.expensetracker.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting data to Excel format using Apache POI.
 *
 * Features:
 * - Professional formatting with styles
 * - Bold headers with background color
 * - Currency formatting for amounts
 * - Auto-sized columns
 * - Multiple sheets support
 */
@Service
@Slf4j
public class ExcelExporter {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export transactions to Excel format (.xlsx).
     *
     * @param transactions list of transactions to export
     * @return Excel file content as byte array
     */
    public byte[] exportTransactionsToExcel(List<Transaction> transactions) {
        log.debug("Exporting {} transactions to Excel", transactions.size());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Create sheet
            Sheet sheet = workbook.createSheet("Transaksi");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Tanggal", "Tipe", "Kategori", "Dompet", "Jumlah (IDR)", "Catatan"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowNum++);

                // Date
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(t.getDate().format(DATE_FORMATTER));
                dateCell.setCellStyle(normalStyle);

                // Type
                Cell typeCell = row.createCell(1);
                typeCell.setCellValue(formatType(t.getType().name()));
                typeCell.setCellStyle(normalStyle);

                // Category
                Cell categoryCell = row.createCell(2);
                categoryCell.setCellValue(t.getCategory() != null ? t.getCategory().getName() : "-");
                categoryCell.setCellStyle(normalStyle);

                // Wallet
                Cell walletCell = row.createCell(3);
                walletCell.setCellValue(t.getWallet() != null ? t.getWallet().getName() : "-");
                walletCell.setCellStyle(normalStyle);

                // Amount
                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(t.getAmount());
                amountCell.setCellStyle(currencyStyle);

                // Note
                Cell noteCell = row.createCell(5);
                noteCell.setCellValue(t.getNote() != null ? t.getNote() : "");
                noteCell.setCellStyle(normalStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary row at the bottom
            addSummaryRow(sheet, transactions, rowNum, headerStyle, currencyStyle);

            workbook.write(out);
            log.info("Successfully exported {} transactions to Excel", transactions.size());
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate Excel", e);
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    /**
     * Create header cell style (bold, gray background).
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Create currency cell style.
     */
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * Create normal cell style.
     */
    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * Add summary row with total income and expense.
     */
    private void addSummaryRow(Sheet sheet, List<Transaction> transactions, int rowNum,
                               CellStyle headerStyle, CellStyle currencyStyle) {
        // Add empty row
        rowNum++;

        // Summary row
        Row summaryRow = sheet.createRow(rowNum);

        Cell labelCell = summaryRow.createCell(3);
        labelCell.setCellValue("TOTAL:");
        labelCell.setCellStyle(headerStyle);

        // Calculate totals
        double totalIncome = transactions.stream()
            .filter(t -> "INCOME".equals(t.getType().name()))
            .mapToDouble(Transaction::getAmount)
            .sum();

        double totalExpense = transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType().name()))
            .mapToDouble(Transaction::getAmount)
            .sum();

        Cell totalCell = summaryRow.createCell(4);
        totalCell.setCellValue(totalIncome - totalExpense);
        totalCell.setCellStyle(currencyStyle);

        Cell noteCell = summaryRow.createCell(5);
        noteCell.setCellValue(String.format("Pemasukan: %.2f | Pengeluaran: %.2f",
            totalIncome, totalExpense));
        noteCell.setCellStyle(headerStyle);
    }

    /**
     * Format transaction type for Indonesian display.
     */
    private String formatType(String type) {
        return switch (type) {
            case "INCOME" -> "Pemasukan";
            case "EXPENSE" -> "Pengeluaran";
            default -> type;
        };
    }
}
