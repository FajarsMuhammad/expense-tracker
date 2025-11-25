package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.category.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAllCategoriesUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FindAllCategoriesUseCase useCase;

    private UUID userId;
    private Category defaultCategory;
    private Category userCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        defaultCategory = Category.builder()
                .id(UUID.randomUUID())
                .user(null)
                .name("Salary")
                .type(CategoryType.INCOME)
                .createdAt(new Date())
                .build();

        userCategory = Category.builder()
                .id(UUID.randomUUID())
                .user(com.fajars.expensetracker.user.User.builder().id(userId).build())
                .name("Freelance")
                .type(CategoryType.INCOME)
                .createdAt(new Date())
                .build();
    }

    @Test
    void findAllByUserId_ShouldReturnDefaultAndUserCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(defaultCategory, userCategory);
        when(categoryRepository.findByUserIdOrUserIdIsNull(userId)).thenReturn(categories);

        // Act
        List<CategoryDto> result = useCase.findAllByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isDefault());
        assertFalse(result.get(1).isDefault());
        verify(categoryRepository).findByUserIdOrUserIdIsNull(userId);
    }

    @Test
    void findAllByUserId_ShouldReturnEmptyList_WhenNoCategories() {
        // Arrange
        when(categoryRepository.findByUserIdOrUserIdIsNull(userId)).thenReturn(Arrays.asList());

        // Act
        List<CategoryDto> result = useCase.findAllByUserId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository).findByUserIdOrUserIdIsNull(userId);
    }
}
