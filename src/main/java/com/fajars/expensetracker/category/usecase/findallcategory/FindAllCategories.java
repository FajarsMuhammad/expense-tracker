package com.fajars.expensetracker.category.usecase.findallcategory;

import com.fajars.expensetracker.category.api.CategoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Find all categories for a user
 * Returns both default system categories and user's custom categories
 */
public interface FindAllCategories {
    List<CategoryResponse> findAllByUserId(UUID userId);
}
