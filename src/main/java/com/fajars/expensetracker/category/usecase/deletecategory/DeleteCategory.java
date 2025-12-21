package com.fajars.expensetracker.category.usecase.deletecategory;

import java.util.UUID;

/**
 * Use Case: Delete a user's custom category
 * Note: Cannot delete default system categories
 */
public interface DeleteCategory {
    void delete(UUID userId, UUID categoryId);
}
