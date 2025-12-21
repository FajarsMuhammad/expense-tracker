package com.fajars.expensetracker.category.usecase.createcategory;

import com.fajars.expensetracker.category.api.CategoryResponse;
import com.fajars.expensetracker.category.api.CreateCategoryRequest;

import java.util.UUID;

/**
 * Use Case: Create a new custom category for a user
 */
public interface CreateCategory {
    CategoryResponse create(UUID userId, CreateCategoryRequest request);
}
