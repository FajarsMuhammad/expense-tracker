package com.fajars.expensetracker.category.api;

import com.fajars.expensetracker.category.domain.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a new category")
public record CreateCategoryRequest(
        @NotBlank(message = "Category name is required")
        @Schema(description = "Category name", example = "Groceries", required = true)
        String name,

        @NotNull(message = "Category type is required")
        @Schema(description = "Category type (INCOME or EXPENSE)", example = "EXPENSE", required = true)
        CategoryType type
) {}
