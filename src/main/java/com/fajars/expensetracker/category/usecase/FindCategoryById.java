package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.CategoryDto;

import java.util.UUID;

/**
 * Use Case: Find a specific category by ID
 * User can access both default categories and their own custom categories
 */
public interface FindCategoryById {
    CategoryDto findByIdAndUserId(UUID categoryId, UUID userId);
}
