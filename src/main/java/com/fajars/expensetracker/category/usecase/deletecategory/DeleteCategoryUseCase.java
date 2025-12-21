package com.fajars.expensetracker.category.usecase.deletecategory;

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
public class DeleteCategoryUseCase implements DeleteCategory {

    private final CategoryRepository categoryRepository;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider userProvider;

    @Override
    @Transactional
    public void delete(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found or cannot be deleted. Default categories cannot be deleted."));

        if (category.isDefault()) {
            throw new IllegalStateException("Cannot delete default categories");
        }

        categoryRepository.delete(category);

        String username = userProvider.getEmail();
        businessEventLogger.logCategoryDeleted(category.getId().getMostSignificantBits(), username);
    }
}
