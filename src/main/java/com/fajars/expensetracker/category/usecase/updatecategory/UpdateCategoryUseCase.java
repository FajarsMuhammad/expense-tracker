package com.fajars.expensetracker.category.usecase.updatecategory;

import com.fajars.expensetracker.category.api.CategoryResponse;
import com.fajars.expensetracker.category.api.UpdateCategoryRequest;
import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase implements UpdateCategory {

    private final CategoryRepository categoryRepository;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Category not found or cannot be modified. Default categories cannot be edited."));

        if (category.isDefault()) {
            throw new IllegalStateException("Cannot edit default categories");
        }

        String oldName = category.getName();
        category.setName(request.name().trim());
        category = categoryRepository.save(category);

        // Log business event
        if (!oldName.equals(category.getName())) {
            String username = currentUserProvider.getEmail();
            businessEventLogger.logCategoryUpdated(
                category.getId().getMostSignificantBits(), username, "name", oldName,
                category.getName()
            );
        }

        return CategoryResponse.from(category);
    }

}
