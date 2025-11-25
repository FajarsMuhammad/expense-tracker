package com.fajars.expensetracker.category;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.UUID;

@Schema(description = "Category response data")
public record CategoryDto(
        @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Category name", example = "Groceries")
        String name,

        @Schema(description = "Category type", example = "EXPENSE")
        CategoryType type,

        @Schema(description = "Whether this is a default system category", example = "false")
        boolean isDefault,

        @Schema(description = "Creation timestamp")
        Date createdAt
) {}
