package com.fajars.expensetracker.category.usecase;

import com.fajars.expensetracker.category.Category;
import com.fajars.expensetracker.category.CategoryDto;
import com.fajars.expensetracker.category.CategoryRepository;
import com.fajars.expensetracker.category.CategoryType;
import com.fajars.expensetracker.category.CreateCategoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private com.fajars.expensetracker.common.metrics.MetricsService metricsService;

    @Mock
    private com.fajars.expensetracker.common.logging.BusinessEventLogger businessEventLogger;

    @InjectMocks
    private CreateCategoryUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void create_ShouldCreateCategory_WhenValidRequest() {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest("Freelance", CategoryType.INCOME);
        Category savedCategory = Category.builder()
                .id(UUID.randomUUID())
                .user(com.fajars.expensetracker.user.User.builder().id(userId).build())
                .name("Freelance")
                .type(CategoryType.INCOME)
                .createdAt(new Date())
                .build();

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // Act
        CategoryDto result = useCase.create(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Freelance", result.name());
        assertEquals(CategoryType.INCOME, result.type());
        assertFalse(result.isDefault());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_ShouldThrowException_WhenNameIsEmpty() {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest("", CategoryType.INCOME);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.create(userId, request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenNameIsNull() {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest(null, CategoryType.INCOME);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.create(userId, request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenTypeIsNull() {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest("Freelance", null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.create(userId, request));
        verify(categoryRepository, never()).save(any());
    }
}
