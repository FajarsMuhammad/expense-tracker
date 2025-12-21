package com.fajars.expensetracker.category.api;

import com.fajars.expensetracker.category.domain.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update an existing category")
public record UpdateCategoryRequest(
    @NotBlank(message = "Category name is required")
    @Schema(description = "New category name", example = "Monthly Groceries", requiredMode = RequiredMode.REQUIRED)
    String name,

    @NotNull(message = "Category type is required")
    @Schema(description = "Category type (INCOME or EXPENSE)", example = "EXPENSE", requiredMode = RequiredMode.REQUIRED)
    CategoryType type
) {

}
