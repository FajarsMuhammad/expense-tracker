package com.fajars.expensetracker.common.config;

import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.category.domain.CategoryRepository;
import com.fajars.expensetracker.category.domain.CategoryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedDefaultCategories();
    }

    private void seedDefaultCategories() {
        // Check if default categories already exist
        List<Category> existingDefaults = categoryRepository.findDefaultCategories();
        if (!existingDefaults.isEmpty()) {
            log.info("Default categories already seeded. Count: {}", existingDefaults.size());
            return;
        }

        log.info("Seeding default categories...");

        // Income categories
        List<Category> incomeCategories = List.of(
                createDefaultCategory("Salary", CategoryType.INCOME),
                createDefaultCategory("Gift", CategoryType.INCOME),
                createDefaultCategory("Bonus", CategoryType.INCOME),
                createDefaultCategory("Other Income", CategoryType.INCOME)
        );

        // Expense categories
        List<Category> expenseCategories = List.of(
                createDefaultCategory("Food & Dining", CategoryType.EXPENSE),
                createDefaultCategory("Shopping", CategoryType.EXPENSE),
                createDefaultCategory("Bills & Utilities", CategoryType.EXPENSE),
                createDefaultCategory("Other Expense", CategoryType.EXPENSE)
        );

        // Save all categories
        categoryRepository.saveAll(incomeCategories);
        categoryRepository.saveAll(expenseCategories);

        log.info("Successfully seeded {} default categories",
                incomeCategories.size() + expenseCategories.size());
    }

    private Category createDefaultCategory(String name, CategoryType type) {
        return Category.builder()
                .id(UUID.randomUUID())
                .user(null) // null user means default category
                .name(name)
                .type(type)
                .createdAt(new Date())
                .build();
    }
}
