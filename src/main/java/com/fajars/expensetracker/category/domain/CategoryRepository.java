package com.fajars.expensetracker.category.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find all categories that are either default (user_id is null) or belong to the specified user
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL ORDER BY c.name")
    List<Category> findByUserIdOrUserIdIsNull(@Param("userId") UUID userId);

    /**
     * Find a category by ID that belongs to the specified user
     * (excludes default categories for edit/delete operations)
     */
    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Find all default categories (system categories)
     */
    @Query("SELECT c FROM Category c WHERE c.user IS NULL ORDER BY c.name")
    List<Category> findDefaultCategories();

    /**
     * Find categories by type for a user (including defaults)
     */
    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.user IS NULL) AND c.type = :type ORDER BY c.name")
    List<Category> findByUserIdOrUserIdIsNullAndType(@Param("userId") UUID userId, @Param("type") CategoryType type);
}
