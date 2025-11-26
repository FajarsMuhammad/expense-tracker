package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.CategoryResponse;
import com.fajars.expensetracker.category.UpdateCategoryRequest;

import java.util.UUID;

/**
 * Use Case: Update a user's custom category
 * Note: Cannot update default system categories
 */
public interface UpdateCategory {
    CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request);
}
