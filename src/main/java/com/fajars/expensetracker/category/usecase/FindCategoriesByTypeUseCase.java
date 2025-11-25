package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.category.CategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindCategoriesByTypeUseCase implements FindCategoriesByType {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findByUserIdAndType(UUID userId, CategoryType type) {
        List<Category> categories = categoryRepository.findByUserIdOrUserIdIsNullAndType(userId, type);
        return categories.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }
}
