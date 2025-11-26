package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryResponse;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.category.UpdateCategoryRequest;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase implements UpdateCategory {

    private final CategoryRepository categoryRepository;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        // Validate name
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        // Find category - this will exclude default categories (user IS NULL)
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found or cannot be modified. Default categories cannot be edited."));

        // Additional check to prevent editing default categories
        if (category.isDefault()) {
            throw new IllegalStateException("Cannot edit default categories");
        }

        String oldName = category.getName();
        category.setName(request.name().trim());
        category = categoryRepository.save(category);

        // Log business event
        if (!oldName.equals(category.getName())) {
            String username = getCurrentUsername();
            businessEventLogger.logCategoryUpdated(
                category.getId().getMostSignificantBits(), username, "name", oldName, category.getName()
            );
        }

        return CategoryResponse.from(category);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
