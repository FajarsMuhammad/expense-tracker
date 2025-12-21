package com.fajars.expensetracker.category.usecase.createcategory;

import com.fajars.expensetracker.category.api.CategoryResponse;
import com.fajars.expensetracker.category.api.CreateCategoryRequest;
import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.user.domain.User;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase implements CreateCategory {

    private final CategoryRepository categoryRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
        // Validate name
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        // Validate type
        if (request.type() == null) {
            throw new IllegalArgumentException("Category type is required (INCOME or EXPENSE)");
        }

        Category category = Category.builder()
            .id(UUID.randomUUID())
            .user(User.builder().id(userId).build())
            .name(request.name().trim())
            .type(request.type())
            .createdAt(new Date())
            .build();

        category = categoryRepository.save(category);

        // Log business event and metrics
        String username = currentUserProvider.getEmail();
        businessEventLogger.logCategoryCreated(category.getId().getMostSignificantBits(), username,
                                               category.getName());
        metricsService.recordCategoryCreated();

        return CategoryResponse.from(category);
    }
}
