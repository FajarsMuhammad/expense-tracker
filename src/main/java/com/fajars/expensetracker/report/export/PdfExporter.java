package com.fajars.expensetracker.report.export;

import com.fajars.expensetracker.transaction.projection.TransactionExportRow;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for exporting data to PDF format using OpenPDF.
 *
 * Features:
 * - Professional layout with header and footer
 * - Table with borders and styling
 * - Summary statistics
 * - Indonesian localization
 */
@Service
@Slf4j
public class PdfExporter {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter REPORT_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMMM yyyy");

    /**
     * Export transactions to PDF format.
     *
     * @param transactions list of transactions to export
     * @return PDF file content as byte array
     */
    public byte[] exportTransactionsToPdf(List<TransactionExportRow> transactions) {
        log.debug("Exporting {} transactions to PDF", transactions.size());

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add title
            addTitle(document);

            // Add generated date
            addGeneratedDate(document);

            // Add spacing
            document.add(new Paragraph(" "));

            // Add summary
            addSummary(document, transactions);

            // Add spacing
            document.add(new Paragraph(" "));

            // Add transaction table
            addTransactionTable(document, transactions);

            document.close();
            log.info("Successfully exported {} transactions to PDF", transactions.size());
            return out.toByteArray();

        } catch (DocumentException e) {
            log.error("Failed to generate PDF", e);
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }

    /**
     * Add title to the document.
     */
    private void addTitle(Document document) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Laporan Transaksi", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
    }

    /**
     * Add generated date.
     */
    private void addGeneratedDate(Document document) throws DocumentException {
        Font dateFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        Paragraph date = new Paragraph(
            "Dibuat pada: " + LocalDateTime.now().format(REPORT_DATE_FORMATTER),
            dateFont
        );
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);
    }

    /**
     * Add summary statistics.
     */
    private void addSummary(Document document, List<TransactionExportRow> transactions) throws DocumentException {
        double totalIncome = transactions.stream()
            .filter(t -> "INCOME".equals(t.type().name()))
            .mapToDouble(TransactionExportRow::amount)
            .sum();

        double totalExpense = transactions.stream()
            .filter(t -> "EXPENSE".equals(t.type().name()))
            .mapToDouble(TransactionExportRow::amount)
            .sum();

        double netBalance = totalIncome - totalExpense;

        Font summaryFont = new Font(Font.HELVETICA, 11, Font.BOLD);

        Paragraph summaryParagraph = new Paragraph();
        summaryParagraph.add(new Chunk("Ringkasan:\n", summaryFont));
        summaryParagraph.add(new Chunk(
            String.format("Total Pemasukan: Rp %,.2f\n", totalIncome),
            new Font(Font.HELVETICA, 10, Font.NORMAL)
        ));
        summaryParagraph.add(new Chunk(
            String.format("Total Pengeluaran: Rp %,.2f\n", totalExpense),
            new Font(Font.HELVETICA, 10, Font.NORMAL)
        ));
        summaryParagraph.add(new Chunk(
            String.format("Saldo Bersih: Rp %,.2f", netBalance),
            new Font(Font.HELVETICA, 10, Font.BOLD)
        ));

        document.add(summaryParagraph);
    }

    /**
     * Add transaction table.
     */
    private void addTransactionTable(Document document, List<TransactionExportRow> transactions)
        throws DocumentException {

        // Create table with 5 columns
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 1.5f, 2f, 1.5f, 2f});

        // Add header
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Color headerColor = new Color(64, 64, 64);

        addHeaderCell(table, "Tanggal", headerFont, headerColor);
        addHeaderCell(table, "Tipe", headerFont, headerColor);
        addHeaderCell(table, "Kategori", headerFont, headerColor);
        addHeaderCell(table, "Jumlah (IDR)", headerFont, headerColor);
        addHeaderCell(table, "Catatan", headerFont, headerColor);

        // Add data rows
        Font dataFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

        for (TransactionExportRow t : transactions) {
            // Date
            addDataCell(table, t.date().format(DATE_FORMATTER), dataFont);

            // Type
            addDataCell(table, formatType(t.type().name()), dataFont);

            // Category
            addDataCell(table,
                t.categoryName() != null ? t.categoryName() : "-",
                dataFont);

            // Amount
            addDataCell(table, String.format("%,.2f", t.amount()), dataFont);

            // Note
            addDataCell(table,
                t.note() != null && !t.note().isEmpty() ? t.note() : "-",
                dataFont);
        }

        document.add(table);
    }

    /**
     * Add header cell to table.
     */
    private void addHeaderCell(PdfPTable table, String text, Font font, Color backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        table.addCell(cell);
    }

    /**
     * Add data cell to table.
     */
    private void addDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
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
