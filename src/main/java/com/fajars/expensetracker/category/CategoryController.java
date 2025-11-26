package com.fajars.expensetracker.category;

import com.fajars.expensetracker.category.usecase.*;
import com.fajars.expensetracker.user.UserDto;
import com.fajars.expensetracker.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs - Manage income and expense categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final FindAllCategories findAllCategories;
    private final FindCategoriesByType findCategoriesByType;
    private final FindCategoryById findCategoryById;
    private final CreateCategory createCategory;
    private final UpdateCategory updateCategory;
    private final DeleteCategory deleteCategory;
    private final UserService userService;

    @Operation(
            summary = "List all categories",
            description = "Get all categories including default system categories and user's custom categories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listCategories(
            @Parameter(description = "Filter by category type (INCOME or EXPENSE)", required = false)
            @RequestParam(required = false) CategoryType type
    ) {
        UUID userId = getCurrentUserId();
        List<CategoryResponse> categories;

        if (type != null) {
            categories = findCategoriesByType.findByUserIdAndType(userId, type);
        } else {
            categories = findAllCategories.findAllByUserId(userId);
        }

        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Get category by ID",
            description = "Get a specific category by its ID. Can access both default categories and user's custom categories."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved category",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Category not found or access denied", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable UUID id
    ) {
        UUID userId = getCurrentUserId();
        CategoryResponse category = findCategoryById.findByIdAndUserId(id, userId);
        return ResponseEntity.ok(category);
    }

    @Operation(
            summary = "Create new category",
            description = "Create a new custom category for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCategoryRequest.class))
            )
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        UUID userId = getCurrentUserId();
        CategoryResponse category = createCategory.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @Operation(
            summary = "Update category",
            description = "Update a user's custom category. Default system categories cannot be modified."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot edit default categories or categories owned by another user", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateCategoryRequest.class))
            )
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        UUID userId = getCurrentUserId();
        CategoryResponse category = updateCategory.update(userId, id, request);
        return ResponseEntity.ok(category);
    }

    @Operation(
            summary = "Delete category",
            description = "Delete a user's custom category. Default system categories cannot be deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot delete default categories or categories owned by another user", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable UUID id
    ) {
        UUID userId = getCurrentUserId();
        deleteCategory.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        String email = auth.getName();
        UserDto user = userService.getByEmail(email);
        return user.id();
    }
}
