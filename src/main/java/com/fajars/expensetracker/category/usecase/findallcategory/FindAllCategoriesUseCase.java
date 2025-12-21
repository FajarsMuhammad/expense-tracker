package com.fajars.expensetracker.category.usecase.findallcategory;

import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.api.CategoryResponse;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindAllCategoriesUseCase implements FindAllCategories {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllByUserId(UUID userId) {
        List<Category> categories = categoryRepository.findByUserIdOrUserIdIsNull(userId);
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
}
