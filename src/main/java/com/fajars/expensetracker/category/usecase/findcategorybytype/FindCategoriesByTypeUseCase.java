package com.fajars.expensetracker.category.usecase.findcategorybytype;

import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.api.CategoryResponse;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import com.fajars.expensetracker.category.domain.CategoryType;
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
    public List<CategoryResponse> findByUserIdAndType(UUID userId, CategoryType type) {
        List<Category> categories = categoryRepository.findByUserIdOrUserIdIsNullAndType(userId, type);
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
}
