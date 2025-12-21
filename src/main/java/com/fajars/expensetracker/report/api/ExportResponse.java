package com.fajars.expensetracker.report.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO for export operations.
 * Contains information about the generated file.
 */
@Schema(description = "Export operation result")
public record ExportResponse(

    @Schema(description = "Generated file name", example = "transactions_2025-12-03.csv")
    String fileName,

    @Schema(description = "File size in bytes", example = "15360")
    Long fileSize,

    @Schema(description = "Download URL (if applicable)")
    String downloadUrl,

    @Schema(description = "Base64 encoded file content (for direct download)")
    String base64Content,

    @Schema(description = "Content type", example = "text/csv")
    String contentType,

    @Schema(description = "When the file expires (for temporary URLs)")
    LocalDateTime expiresAt
) {
}
