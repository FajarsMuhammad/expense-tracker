package com.fajars.expensetracker.report.export;

import com.fajars.expensetracker.transaction.Transaction;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting data to CSV format using OpenCSV.
 *
 * Features:
 * - Clean CSV output with proper headers
 * - UTF-8 encoding for international characters
 * - Formatted numbers and dates
 * - Safe handling of null values
 */
@Service
@Slf4j
public class CsvExporter {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export transactions to CSV format.
     *
     * @param transactions list of transactions to export
     * @return CSV file content as byte array
     */
    public byte[] exportTransactionsToCsv(List<Transaction> transactions) {
        log.debug("Exporting {} transactions to CSV", transactions.size());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer,
                 CSVWriter.DEFAULT_SEPARATOR,
                 CSVWriter.NO_QUOTE_CHARACTER,
                 CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                 CSVWriter.DEFAULT_LINE_END)) {

            // Write header
            String[] header = {
                "Tanggal",
                "Tipe",
                "Kategori",
                "Dompet",
                "Jumlah (IDR)",
                "Catatan"
            };
            csvWriter.writeNext(header);

            // Write data rows
            for (Transaction t : transactions) {
                String[] row = {
                    t.getDate().format(DATE_FORMATTER),
                    formatType(t.getType().name()),
                    t.getCategory() != null ? t.getCategory().getName() : "-",
                    t.getWallet() != null ? t.getWallet().getName() : "-",
                    formatAmount(t.getAmount()),
                    t.getNote() != null ? t.getNote() : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            log.info("Successfully exported {} transactions to CSV", transactions.size());
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate CSV", e);
            throw new RuntimeException("Failed to generate CSV export", e);
        }
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

    /**
     * Format amount with Indonesian number format (using dot as thousand separator).
     */
    private String formatAmount(Double amount) {
        if (amount == null) {
            return "0";
        }
        return String.format("%,.2f", amount).replace(',', '.');
    }
}
