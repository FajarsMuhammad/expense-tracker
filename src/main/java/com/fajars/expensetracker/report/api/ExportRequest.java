package com.fajars.expensetracker.report.api;

import com.fajars.expensetracker.report.usecase.exporttransaction.ExportFilter;
import com.fajars.expensetracker.report.usecase.exporttransaction.ExportFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for exporting data.
 */
@Schema(description = "Request to export data in various formats")
public record ExportRequest(

    @NotNull(message = "Export format is required")
    @Schema(description = "Export file format", example = "CSV")
    ExportFormat format,

    @NotNull(message = "Export type is required")
    @Schema(description = "Type of data to export", example = "TRANSACTIONS")
    ExportType type,

    @Schema(description = "Filter criteria for the export")
    ExportFilter filter
) {
}
