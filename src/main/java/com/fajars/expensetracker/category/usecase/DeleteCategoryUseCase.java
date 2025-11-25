package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase implements DeleteCategory {

    private final CategoryRepository categoryRepository;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public void delete(UUID userId, UUID categoryId) {
        // Find category - this will exclude default categories
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found or cannot be deleted. Default categories cannot be deleted."));

        // Additional check to prevent deleting default categories
        if (category.isDefault()) {
            throw new IllegalStateException("Cannot delete default categories");
        }

        categoryRepository.delete(category);

        // Log business event
        String username = getCurrentUsername();
        businessEventLogger.logCategoryDeleted(category.getId().getMostSignificantBits(), username);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
