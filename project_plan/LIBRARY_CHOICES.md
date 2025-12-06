# Library Choices for Milestone 4: Reports & Export

## 📊 Export Libraries - Final Decisions

### ✅ CSV: OpenCSV (Recommended)

**Why OpenCSV over Apache Commons CSV:**

| Feature | OpenCSV ✅ | Apache Commons CSV |
|---------|-----------|-------------------|
| **Popularity** | 4.5k+ GitHub stars | Part of Apache Commons |
| **Bean Mapping** | ✅ Full annotation support | ❌ No annotations |
| **API Simplicity** | ✅ Very intuitive | ⚠️ More verbose |
| **Type Conversion** | ✅ Built-in converters | ❌ Manual |
| **Custom Headers** | ✅ `@CsvBindByName` | ⚠️ Manual setup |
| **Date Formatting** | ✅ `@CsvDate` annotation | ❌ Manual |
| **Number Formatting** | ✅ `@CsvNumber` annotation | ❌ Manual |
| **Active Development** | ✅ Very active | ✅ Active |
| **Documentation** | ✅ Excellent | ⚠️ Good |

**Code Comparison:**

```java
// OpenCSV (Simple & Clean)
@CsvBindByName(column = "Amount")
@CsvNumber("#0.00")
private Double amount;

// Apache Commons CSV (More Manual)
CSVFormat.DEFAULT.withHeader("Amount")
printer.printRecord(String.format("%.2f", amount))
```

**Maven Dependency:**
```gradle
implementation 'com.opencsv:opencsv:5.9'
```

**License:** Apache 2.0 ✅ (Free for commercial use)

---

### ✅ Excel: Apache POI (Recommended)

**Why Apache POI:**

- ✅ **Industry Standard** - Most popular Java Excel library
- ✅ **Full Features** - Read/write XLS and XLSX
- ✅ **Apache 2.0 License** - Truly free for commercial use
- ✅ **Rich Formatting** - Styles, formulas, charts
- ✅ **Active Development** - Regular updates
- ✅ **Great Documentation** - Extensive examples

**Alternatives Considered:**
- **JExcelApi** - Outdated, no XLSX support
- **FastExcel** - Lighter but less features
- **Apache POI** - ✅ Best choice

**Maven Dependencies:**
```gradle
implementation 'org.apache.poi:poi:5.2.5'
implementation 'org.apache.poi:poi-ooxml:5.2.5'
```

**License:** Apache 2.0 ✅ (Free for commercial use)

---

### ✅ PDF: OpenPDF (Recommended)

**Why OpenPDF over iText:**

| Feature | OpenPDF ✅ | iText 7 | Apache PDFBox |
|---------|-----------|---------|---------------|
| **License** | LGPL/MPL | AGPL ⚠️ | Apache 2.0 ✅ |
| **Commercial Use** | ✅ Free | ❌ Requires license | ✅ Free |
| **API Complexity** | ✅ Easy (iText 4 fork) | ⚠️ Complex | ⚠️ Complex |
| **Documentation** | ✅ Good | ✅ Excellent | ⚠️ Good |
| **Tables/Layout** | ✅ Easy | ✅ Easy | ⚠️ Manual |
| **Active Development** | ✅ Active | ✅ Very active | ✅ Active |
| **Learning Curve** | ✅ Easy | ⚠️ Steep | ⚠️ Steep |

**License Comparison:**

**iText 7 (⚠️ Avoid for Commercial):**
```
AGPL 3.0 - Requires:
- Open source your entire application
- OR purchase commercial license ($$$)
```

**OpenPDF (✅ Recommended):**
```
LGPL/MPL - Allows:
- Commercial use WITHOUT licensing
- Closed source applications ✅
- No cost ✅
```

**Apache PDFBox (✅ Alternative):**
```
Apache 2.0 - Truly free
BUT: More complex API, harder to learn
```

**Maven Dependency:**
```gradle
implementation 'com.github.librepdf:openpdf:2.0.2'
```

**License:** LGPL/MPL ✅ (Free for commercial use)

---

## 🎯 Final Recommendation

### For Your Expense Tracker UMKM Project:

```gradle
dependencies {
    // CSV Export - OpenCSV (best features + ease of use)
    implementation 'com.opencsv:opencsv:5.9'

    // Excel Export - Apache POI (industry standard)
    implementation 'org.apache.poi:poi:5.2.5'
    implementation 'org.apache.poi:poi-ooxml:5.2.5'

    // PDF Export - OpenPDF (free for commercial, easy API)
    implementation 'com.github.librepdf:openpdf:2.0.2'
}
```

**All licenses are commercial-friendly! ✅**

---

## 📚 Code Examples

### OpenCSV - Bean Mapping (Advanced)

```java
// 1. Define export DTO with annotations
@Data
public class TransactionExportDto {
    @CsvBindByName(column = "Tanggal")
    @CsvDate("dd/MM/yyyy")
    private LocalDateTime date;

    @CsvBindByName(column = "Tipe")
    private String type;

    @CsvBindByName(column = "Jumlah")
    @CsvNumber("#,##0.00")
    private Double amount;
}

// 2. Export with ONE line
StatefulBeanToCsv<TransactionExportDto> writer =
    new StatefulBeanToCsvBuilder<>(outputWriter).build();
writer.write(dtoList);
```

### Apache POI - Excel with Formatting

```java
XSSFWorkbook workbook = new XSSFWorkbook();
XSSFSheet sheet = workbook.createSheet("Transactions");

// Create header style (bold, colored)
CellStyle headerStyle = workbook.createCellStyle();
Font font = workbook.createFont();
font.setBold(true);
headerStyle.setFont(font);
headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// Create currency style
CellStyle currencyStyle = workbook.createCellStyle();
currencyStyle.setDataFormat(
    workbook.createDataFormat().getFormat("#,##0.00")
);

// Create rows with styled cells
// ...add data with styles...
```

### OpenPDF - Professional Layout

```java
Document document = new Document(PageSize.A4);
PdfWriter.getInstance(document, outputStream);
document.open();

// Add title
Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
Paragraph title = new Paragraph("Laporan Keuangan", titleFont);
title.setAlignment(Element.ALIGN_CENTER);
document.add(title);

// Add table
PdfPTable table = new PdfPTable(4);
table.addCell("Tanggal");
table.addCell("Kategori");
table.addCell("Jumlah");
table.addCell("Catatan");
// ...add rows...

document.close();
```

---

## ⚠️ Important Notes

### Commercial Use

**Safe to Use ✅:**
- OpenCSV (Apache 2.0)
- Apache POI (Apache 2.0)
- OpenPDF (LGPL - can link dynamically)

**Avoid ⚠️:**
- iText 7 (AGPL - requires licensing for commercial)
- Any GPL-licensed libraries

### Indonesian Localization

For Indonesian number formatting (1.000.000,00):

```java
// Use Indonesian locale
Locale indonesianLocale = new Locale("id", "ID");
NumberFormat formatter = NumberFormat.getInstance(indonesianLocale);

// For CSV
@CsvNumber(value = "#,##0.00", locale = "id-ID")
private Double amount;

// For Excel
CellStyle style = workbook.createCellStyle();
style.setDataFormat(
    workbook.createDataFormat().getFormat("#,##0.00")
);
```

---

## 🚀 Performance Tips

**OpenCSV:**
- Use `CSVWriter` for simple exports
- Use `StatefulBeanToCsv` for complex bean mapping
- Write directly to OutputStream (avoid String concatenation)

**Apache POI:**
- Use `SXSSFWorkbook` for large datasets (streaming)
- Flush rows to disk periodically
- Limit cell styles (reuse style objects)

**OpenPDF:**
- Add content incrementally (don't build in memory)
- Use `PdfPTable` for structured data
- Compress images before embedding

---

## 📖 Documentation Links

**OpenCSV:**
- GitHub: https://github.com/opencsv/opencsv
- Documentation: http://opencsv.sourceforge.net/

**Apache POI:**
- Website: https://poi.apache.org/
- Quick Guide: https://poi.apache.org/components/spreadsheet/quick-guide.html

**OpenPDF:**
- GitHub: https://github.com/LibrePDF/OpenPDF
- Examples: https://github.com/LibrePDF/OpenPDF/tree/master/pdf-toolbox/src/test/java/com/lowagie/examples

---

**Last Updated:** December 2025
**Status:** ✅ Production Ready
