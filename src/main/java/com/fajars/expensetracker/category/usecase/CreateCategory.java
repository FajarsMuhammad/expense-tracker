package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CreateCategoryRequest;

import java.util.UUID;

/**
 * Use Case: Create a new custom category for a user
 */
public interface CreateCategory {
    CategoryDto create(UUID userId, CreateCategoryRequest request);
}
