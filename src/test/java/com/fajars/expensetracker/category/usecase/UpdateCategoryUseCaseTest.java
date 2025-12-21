package com.fajars.expensetracker.category.usecase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import com.fajars.expensetracker.category.api.CategoryResponse;
import com.fajars.expensetracker.category.domain.CategoryType;
import com.fajars.expensetracker.category.api.UpdateCategoryRequest;
import com.fajars.expensetracker.category.usecase.updatecategory.UpdateCategoryUseCase;
import com.fajars.expensetracker.user.domain.User;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private com.fajars.expensetracker.common.logging.BusinessEventLogger businessEventLogger;

    @InjectMocks
    private UpdateCategoryUseCase useCase;

    private UUID userId;
    private UUID categoryId;
    private Category userCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        userCategory = Category.builder()
                .id(categoryId)
                .user(User.builder().id(userId).build())
                .name("Freelance")
                .type(CategoryType.INCOME)
                .createdAt(new Date())
                .build();
    }

    @Test
    void update_ShouldUpdateCategory_WhenValidRequest() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name", CategoryType.EXPENSE);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(userCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(userCategory);

        // Act
        CategoryResponse result = useCase.update(userId, categoryId, request);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findByIdAndUserId(categoryId, userId);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name", CategoryType.EXPENSE);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.update(userId, categoryId, request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenNameIsEmpty() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest("", CategoryType.EXPENSE);
        // Note: No need to stub findByIdAndUserId since validation happens before repository call

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.update(userId, categoryId, request));
        verify(categoryRepository, never()).findByIdAndUserId(any(), any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenCategoryIsDefault() {
        // Arrange
        Category defaultCategory = Category.builder()
                .id(categoryId)
                .user(null)
                .name("Salary")
                .type(CategoryType.INCOME)
                .createdAt(new Date())
                .build();
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name", CategoryType.EXPENSE);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(defaultCategory));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> useCase.update(userId, categoryId, request));
        verify(categoryRepository, never()).save(any());
    }
}
