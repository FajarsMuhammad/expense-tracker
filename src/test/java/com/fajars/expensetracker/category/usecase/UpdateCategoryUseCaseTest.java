package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.category.CategoryType;
import com.fajars.expensetracker.category.UpdateCategoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

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
                .user(com.fajars.expensetracker.user.User.builder().id(userId).build())
                .name("Freelance")
                .type(CategoryType.INCOME)
                .createdAt(new Date())
                .build();
    }

    @Test
    void update_ShouldUpdateCategory_WhenValidRequest() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name");
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(userCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(userCategory);

        // Act
        CategoryDto result = useCase.update(userId, categoryId, request);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findByIdAndUserId(categoryId, userId);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name");
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.update(userId, categoryId, request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenNameIsEmpty() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.update(userId, categoryId, request));
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
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name");
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(defaultCategory));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> useCase.update(userId, categoryId, request));
        verify(categoryRepository, never()).save(any());
    }
}
