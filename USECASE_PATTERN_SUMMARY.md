# UseCase Pattern Implementation Summary

## Overview
Project ini telah di-refactor dari **Service Layer pattern** menjadi **UseCase pattern** untuk menerapkan **Single Responsibility Principle** (SOLID).

## Architecture Pattern

### Before (Service Layer):
```
Controller → Service → Repository
```
- Service class memiliki banyak method untuk berbagai operasi
- Satu class bertanggung jawab untuk banyak hal (violation of SRP)

### After (UseCase Pattern):
```
Controller → UseCase Interface → UseCase Implementation → Repository
```
- Setiap use case adalah class terpisah dengan satu tanggung jawab
- Interface dan implementation terpisah untuk flexibility
- Nama class dan method yang descriptive

---

## Implementation Details

### 1. **Category Module** ✅

**Location:** `src/main/java/com/fajars/expensetracker/category/usecase/`

**Use Cases Implemented:**

| Interface | Implementation | Responsibility |
|-----------|---------------|----------------|
| `FindAllCategories` | `FindAllCategoriesUseCase` | List all categories (default + user) |
| `FindCategoriesByType` | `FindCategoriesByTypeUseCase` | Filter categories by type (INCOME/EXPENSE) |
| `FindCategoryById` | `FindCategoryByIdUseCase` | Get specific category by ID |
| `CreateCategory` | `CreateCategoryUseCase` | Create new custom category |
| `UpdateCategory` | `UpdateCategoryUseCase` | Update user's category |
| `DeleteCategory` | `DeleteCategoryUseCase` | Delete user's category |

**Example:**
```java
// Interface
public interface FindAllCategories {
    List<CategoryDto> findAllByUserId(UUID userId);
}

// Implementation
@Service
@RequiredArgsConstructor
public class FindAllCategoriesUseCase implements FindAllCategories {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAllByUserId(UUID userId) {
        // Implementation
    }
}

// Controller usage
@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final FindAllCategories findAllCategories;
    private final CreateCategory createCategory;
    // ... other use cases

    @GetMapping
    public ResponseEntity<List<CategoryDto>> listCategories() {
        List<CategoryDto> categories = findAllCategories.findAllByUserId(userId);
        return ResponseEntity.ok(categories);
    }
}
```

---

### 2. **Wallet Module** ✅

**Location:** `src/main/java/com/fajars/expensetracker/wallet/usecase/`

**Use Cases Implemented:**

| Interface | Implementation | Responsibility |
|-----------|---------------|----------------|
| `FindAllWallets` | `FindAllWalletsUseCase` | List all user's wallets |
| `FindWalletById` | `FindWalletByIdUseCase` | Get specific wallet by ID |
| `CreateWallet` | `CreateWalletUseCase` | Create new wallet (enforce 1 wallet limit for free users) |
| `UpdateWallet` | `UpdateWalletUseCase` | Update wallet details |
| `DeleteWallet` | `DeleteWalletUseCase` | Delete wallet |

**Key Features:**
- Balance calculation logic in DTO mapping
- Free user wallet limit enforcement (1 wallet max)
- Ownership validation

---

### 3. **Dashboard Module** ✅

**Location:** `src/main/java/com/fajars/expensetracker/dashboard/usecase/`

**Use Cases Implemented:**

| Interface | Implementation | Responsibility |
|-----------|---------------|----------------|
| `GetDashboardSummary` | `GetDashboardSummaryUseCase` | Get financial summary with trends |

**Features:**
- Calculate wallet balance
- Today's income/expense
- 7-day weekly trend
- Recent transactions (top 5)
- Support filtering by wallet ID

---

## Benefits of UseCase Pattern

### 1. **Single Responsibility Principle (SRP)**
- ✅ Each use case has ONE job
- ✅ Easy to understand what each class does
- ✅ Changes in one use case don't affect others

### 2. **Better Testability**
- ✅ Each use case can be tested independently
- ✅ Mock only what you need for specific use case
- ✅ Test files are focused and clear

### 3. **Better Maintainability**
- ✅ Easy to find where specific business logic lives
- ✅ Adding new feature = adding new use case (no modification to existing code)
- ✅ Clear separation of concerns

### 4. **Better Readability**
- ✅ Descriptive interface names: `CreateCategory`, `DeleteWallet`, etc.
- ✅ Method names clearly state what they do
- ✅ No confusion about what a class is responsible for

### 5. **Dependency Injection Benefits**
- ✅ Controller only injects use cases it needs
- ✅ Clear dependencies visible in constructor
- ✅ Easy to mock for testing

---

## Naming Conventions

### Interface Names:
- Use **verb + noun** pattern
- Examples: `FindAllCategories`, `CreateWallet`, `DeleteTransaction`
- Clear, action-oriented names

### Implementation Names:
- Interface name + `UseCase` suffix
- Examples: `FindAllCategoriesUseCase`, `CreateWalletUseCase`
- Consistent across all modules

### Method Names:
- Descriptive action verbs
- Examples: `findAllByUserId()`, `create()`, `update()`, `delete()`
- Match the responsibility of the use case

---

## Unit Test Coverage

### Test Files Created:

**Category Module:**
- `FindAllCategoriesUseCaseTest.java`
- `CreateCategoryUseCaseTest.java`
- `UpdateCategoryUseCaseTest.java`
- `DeleteCategoryUseCaseTest.java`

**Wallet Module:**
- `FindAllWalletsUseCaseTest.java`
- `CreateWalletUseCaseTest.java`
- `UpdateWalletUseCaseTest.java`
- `DeleteWalletUseCaseTest.java`

**Dashboard Module:**
- `GetDashboardSummaryUseCaseTest.java`

### Test Coverage:
- ✅ **Happy path scenarios** - Valid requests succeed
- ✅ **Validation scenarios** - Invalid inputs throw exceptions
- ✅ **Business rule scenarios** - Business logic enforced
- ✅ **Edge cases** - Empty results, not found, etc.

**Total Tests:** 20+ test cases covering all use cases

---

## Migration Notes

### Old Service Files (Removed):
- ❌ `CategoryService.java` - Replaced with 6 use cases
- ❌ `WalletService.java` - Replaced with 5 use cases
- ❌ `DashboardService.java` - Replaced with 1 use case

### Controllers Updated:
- ✅ `CategoryController.java` - Now injects 6 use case interfaces
- ✅ `WalletController.java` - Now injects 5 use case interfaces
- ✅ `DashboardController.java` - Now injects 1 use case interface

---

## How to Add New Use Case

### Step 1: Create Interface
```java
package com.fajars.expensetracker.module.usecase;

public interface DoSomething {
    OutputDto execute(UUID userId, InputRequest request);
}
```

### Step 2: Create Implementation
```java
package com.fajars.expensetracker.module.usecase;

@Service
@RequiredArgsConstructor
public class DoSomethingUseCase implements DoSomething {
    private final SomeRepository repository;

    @Override
    @Transactional
    public OutputDto execute(UUID userId, InputRequest request) {
        // Business logic here
    }
}
```

### Step 3: Inject in Controller
```java
@RestController
@RequiredArgsConstructor
public class ModuleController {
    private final DoSomething doSomething;

    @PostMapping("/something")
    public ResponseEntity<OutputDto> doSomething(@RequestBody InputRequest request) {
        OutputDto result = doSomething.execute(userId, request);
        return ResponseEntity.ok(result);
    }
}
```

### Step 4: Write Tests
```java
@ExtendWith(MockitoExtension.class)
class DoSomethingUseCaseTest {
    @Mock
    private SomeRepository repository;

    @InjectMocks
    private DoSomethingUseCase useCase;

    @Test
    void execute_ShouldSucceed_WhenValidRequest() {
        // Test implementation
    }
}
```

---

## Comparison: Before vs After

### Before (Service Pattern):
```java
@Service
public class CategoryService {
    // 6 methods in one class
    public List<CategoryDto> listCategories(...) { }
    public List<CategoryDto> listCategoriesByType(...) { }
    public CategoryDto getCategory(...) { }
    public CategoryDto createCategory(...) { }
    public CategoryDto updateCategory(...) { }
    public void deleteCategory(...) { }
}
```

### After (UseCase Pattern):
```java
// 6 separate use cases, each with its own responsibility
public interface FindAllCategories { ... }
public class FindAllCategoriesUseCase implements FindAllCategories { ... }

public interface FindCategoriesByType { ... }
public class FindCategoriesByTypeUseCase implements FindCategoriesByType { ... }

public interface FindCategoryById { ... }
public class FindCategoryByIdUseCase implements FindCategoryById { ... }

public interface CreateCategory { ... }
public class CreateCategoryUseCase implements CreateCategory { ... }

public interface UpdateCategory { ... }
public class UpdateCategoryUseCase implements UpdateCategory { ... }

public interface DeleteCategory { ... }
public class DeleteCategoryUseCase implements DeleteCategory { ... }
```

---

## Build & Test Status

- ✅ **Build:** SUCCESSFUL
- ✅ **Compilation:** No errors
- ✅ **Unit Tests:** 20+ tests PASSING
- ✅ **Code Quality:** Clean, organized, SOLID principles applied

---

## Future Modules (To Follow Same Pattern)

When implementing Transaction module or other modules, follow this UseCase pattern:

### Transaction Module (Planned):
- `FindAllTransactions` / `FindAllTransactionsUseCase`
- `FindTransactionById` / `FindTransactionByIdUseCase`
- `CreateTransaction` / `CreateTransactionUseCase`
- `UpdateTransaction` / `UpdateTransactionUseCase`
- `DeleteTransaction` / `DeleteTransactionUseCase`

**Remember:** Each use case = One responsibility = One class 🎯

---

**Last Updated:** November 24, 2025
**Pattern Status:** ✅ Fully Implemented across Category, Wallet, and Dashboard modules
