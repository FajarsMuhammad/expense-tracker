package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.CategoryResponse;
import com.fajars.expensetracker.category.CreateCategoryRequest;

import java.util.UUID;

/**
 * Use Case: Create a new custom category for a user
 */
public interface CreateCategory {
    CategoryResponse create(UUID userId, CreateCategoryRequest request);
}
