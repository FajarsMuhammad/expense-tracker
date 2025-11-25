package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.category.CreateCategoryRequest;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase implements CreateCategory {

    private final CategoryRepository categoryRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public CategoryDto create(UUID userId, CreateCategoryRequest request) {
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
        String username = getCurrentUsername();
        businessEventLogger.logCategoryCreated(category.getId().getMostSignificantBits(), username, category.getName());
        metricsService.recordCategoryCreated();

        return toDto(category);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getType(),
                category.isDefault(),
                category.getCreatedAt()
        );
    }
}
