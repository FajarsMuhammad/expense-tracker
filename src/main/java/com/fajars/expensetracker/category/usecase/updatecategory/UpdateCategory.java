package com.fajars.expensetracker.category.usecase.updatecategory;

import com.fajars.expensetracker.category.api.CategoryResponse;
import com.fajars.expensetracker.category.api.UpdateCategoryRequest;

import java.util.UUID;

/**
 * Use Case: Update a user's custom category
 * Note: Cannot update default system categories
 */
public interface UpdateCategory {
    CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request);
}
