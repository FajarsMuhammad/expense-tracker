package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindCategoryByIdUseCase implements FindCategoryById {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findByIdAndUserId(UUID categoryId, UUID userId) {
        // First try to find as user category
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElse(null);

        // If not found, check if it's a default category
        if (category == null) {
            category = categoryRepository.findById(categoryId)
                    .filter(Category::isDefault)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));
        }

        return CategoryDto.from(category);
    }
}
