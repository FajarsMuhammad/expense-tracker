package com.fajars.expensetracker.category.usecase.findcategorybytype;

import com.fajars.expensetracker.category.api.CategoryResponse;
import com.fajars.expensetracker.category.domain.CategoryType;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Find categories filtered by type (INCOME or EXPENSE)
 */
public interface FindCategoriesByType {
    List<CategoryResponse> findByUserIdAndType(UUID userId, CategoryType type);
}
