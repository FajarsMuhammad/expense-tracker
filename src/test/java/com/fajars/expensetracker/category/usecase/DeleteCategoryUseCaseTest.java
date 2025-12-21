package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import com.fajars.expensetracker.category.domain.CategoryType;
import com.fajars.expensetracker.category.usecase.deletecategory.DeleteCategoryUseCase;
import com.fajars.expensetracker.user.domain.User;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private com.fajars.expensetracker.common.logging.BusinessEventLogger businessEventLogger;

    @InjectMocks
    private DeleteCategoryUseCase useCase;

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
    void delete_ShouldDeleteCategory_WhenValidRequest() {
        // Arrange
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(userCategory));

        // Act
        useCase.delete(userId, categoryId);

        // Assert
        verify(categoryRepository).findByIdAndUserId(categoryId, userId);
        verify(categoryRepository).delete(userCategory);
    }

    @Test
    void delete_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.delete(userId, categoryId));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowException_WhenCategoryIsDefault() {
        // Arrange
        Category defaultCategory = Category.builder()
                .id(categoryId)
                .user(null)
                .name("Salary")
                .type(CategoryType.INCOME)
                .createdAt(new Date())
                .build();
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(defaultCategory));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> useCase.delete(userId, categoryId));
        verify(categoryRepository, never()).delete(any());
    }
}
