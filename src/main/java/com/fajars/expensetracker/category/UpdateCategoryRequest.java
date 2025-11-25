package com.fajars.expensetracker.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update an existing category")
public record UpdateCategoryRequest(
        @NotBlank(message = "Category name is required")
        @Schema(description = "New category name", example = "Monthly Groceries", required = true)
        String name
) {}
