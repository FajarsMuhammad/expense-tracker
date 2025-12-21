package com.fajars.expensetracker.category.api;

import com.fajars.expensetracker.category.domain.CategoryType;
import com.fajars.expensetracker.category.usecase.createcategory.CreateCategory;
import com.fajars.expensetracker.category.usecase.deletecategory.DeleteCategory;
import com.fajars.expensetracker.category.usecase.findallcategory.FindAllCategories;
import com.fajars.expensetracker.category.usecase.findcategorybyid.FindCategoryById;
import com.fajars.expensetracker.category.usecase.findcategorybytype.FindCategoriesByType;
import com.fajars.expensetracker.category.usecase.updatecategory.UpdateCategory;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
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
    private final CurrentUserProvider currentUserProvider;

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
        UUID userId = currentUserProvider.getUserId();
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
        UUID userId = currentUserProvider.getUserId();
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
        UUID userId = currentUserProvider.getUserId();
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
        UUID userId = currentUserProvider.getUserId();
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
        UUID userId = currentUserProvider.getUserId();
        deleteCategory.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

}
