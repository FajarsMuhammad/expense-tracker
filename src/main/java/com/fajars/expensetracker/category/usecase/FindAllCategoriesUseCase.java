package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CategoryRepository;
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
    public List<CategoryDto> findAllByUserId(UUID userId) {
        List<Category> categories = categoryRepository.findByUserIdOrUserIdIsNull(userId);
        return categories.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }
}
