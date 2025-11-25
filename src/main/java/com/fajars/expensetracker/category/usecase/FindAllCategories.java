package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.CategoryDto;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Find all categories for a user
 * Returns both default system categories and user's custom categories
 */
public interface FindAllCategories {
    List<CategoryDto> findAllByUserId(UUID userId);
}
