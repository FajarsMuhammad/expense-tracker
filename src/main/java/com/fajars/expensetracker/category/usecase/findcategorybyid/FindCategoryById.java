package com.fajars.expensetracker.category.usecase.findcategorybyid;

import com.fajars.expensetracker.category.api.CategoryResponse;

import java.util.UUID;

/**
 * Use Case: Find a specific category by ID
 * User can access both default categories and their own custom categories
 */
public interface FindCategoryById {
    CategoryResponse findByIdAndUserId(UUID categoryId, UUID userId);
}
