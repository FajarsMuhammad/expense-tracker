package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CategoryType;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Find categories filtered by type (INCOME or EXPENSE)
 */
public interface FindCategoriesByType {
    List<CategoryDto> findByUserIdAndType(UUID userId, CategoryType type);
}
