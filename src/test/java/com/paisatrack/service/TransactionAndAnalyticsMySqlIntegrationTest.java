package com.paisatrack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.paisatrack.dto.AnalyticsDto;
import com.paisatrack.dto.DashboardSummaryDto;
import com.paisatrack.dto.RegisterRequest;
import com.paisatrack.dto.TransactionForm;
import com.paisatrack.dto.TransactionViewDto;
import com.paisatrack.entity.Category;
import com.paisatrack.entity.CategoryType;
import com.paisatrack.entity.User;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Real MySQL integration tests for Transaction CRUD, category-type validation,
 * user data isolation, dashboard balance calculations, and analytics aggregations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionAndAnalyticsMySqlIntegrationTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private EntityManager entityManager;

    private User userA;
    private User userB;
    private Category foodExpenseCategory;
    private Category travelExpenseCategory;
    private Category salaryIncomeCategory;

    @BeforeEach
    void setUp() {
        userA = createTestUser("User A");
        userB = createTestUser("User B");

        List<Category> expenses = categoryService.getCategoriesByType(CategoryType.EXPENSE);
        List<Category> incomes = categoryService.getCategoriesByType(CategoryType.INCOME);

        foodExpenseCategory = expenses.stream()
            .filter(c -> "Food".equalsIgnoreCase(c.getCategoryName()))
            .findFirst()
            .orElse(expenses.get(0));

        travelExpenseCategory = expenses.stream()
            .filter(c -> "Travel".equalsIgnoreCase(c.getCategoryName()))
            .findFirst()
            .orElse(expenses.get(Math.min(1, expenses.size() - 1)));

        salaryIncomeCategory = incomes.stream()
            .filter(c -> "Salary".equalsIgnoreCase(c.getCategoryName()))
            .findFirst()
            .orElse(incomes.get(0));
    }

    @Test
    void crudOperationsPersistInMySqlAndSupportChangingTransactionType() {
        LocalDate today = LocalDate.now(IST);

        TransactionForm expenseForm = new TransactionForm();
        expenseForm.setTransactionType(CategoryType.EXPENSE);
        expenseForm.setCategoryId(foodExpenseCategory.getCategoryId());
        expenseForm.setAmount(new BigDecimal("450.50"));
        expenseForm.setTransactionDate(today);
        expenseForm.setDescription("Lunch");

        transactionService.createTransaction(userA.getUserId(), expenseForm);
        entityManager.flush();
        entityManager.clear();

        List<TransactionViewDto> list = transactionService.getFilteredTransactions(
            userA.getUserId(), null, null
        );
        assertEquals(1, list.size());
        Long txId = list.get(0).getTransactionId();
        assertEquals(CategoryType.EXPENSE, list.get(0).getType());
        assertEquals(new BigDecimal("450.50"), list.get(0).getAmount());

        // Edit and switch type from EXPENSE -> INCOME
        TransactionForm updateToIncome = new TransactionForm();
        updateToIncome.setTransactionId(txId);
        updateToIncome.setTransactionType(CategoryType.INCOME);
        updateToIncome.setCategoryId(salaryIncomeCategory.getCategoryId());
        updateToIncome.setAmount(new BigDecimal("15000.00"));
        updateToIncome.setTransactionDate(today);
        updateToIncome.setDescription("Stipend");

        transactionService.updateTransaction(userA.getUserId(), txId, updateToIncome);
        entityManager.flush();
        entityManager.clear();

        List<TransactionViewDto> updated = transactionService.getFilteredTransactions(
            userA.getUserId(), CategoryType.INCOME, null
        );
        assertEquals(1, updated.size());
        assertEquals(CategoryType.INCOME, updated.get(0).getType());
        assertEquals(new BigDecimal("15000.00"), updated.get(0).getAmount());

        // Delete transaction
        Long updatedId = updated.get(0).getTransactionId();
        transactionService.deleteTransaction(userA.getUserId(), updatedId);
        entityManager.flush();
        entityManager.clear();

        assertTrue(transactionService.getFilteredTransactions(userA.getUserId(), null, null).isEmpty());
    }

    @Test
    void rejectsMismatchedCategoryAndTransactionType() {
        TransactionForm invalid = new TransactionForm();
        invalid.setTransactionType(CategoryType.INCOME);
        invalid.setCategoryId(foodExpenseCategory.getCategoryId()); // EXPENSE category!
        invalid.setAmount(new BigDecimal("500.00"));
        invalid.setTransactionDate(LocalDate.now(IST));

        assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.createTransaction(userA.getUserId(), invalid)
        );
    }

    @Test
    void enforcesStrictUserDataIsolation() {
        TransactionForm form = new TransactionForm();
        form.setTransactionType(CategoryType.EXPENSE);
        form.setCategoryId(foodExpenseCategory.getCategoryId());
        form.setAmount(new BigDecimal("250.00"));
        form.setTransactionDate(LocalDate.now(IST));
        form.setDescription("Private snack");
        transactionService.createTransaction(userA.getUserId(), form);
        entityManager.flush();

        Long userATxId = transactionService
            .getFilteredTransactions(userA.getUserId(), null, null)
            .get(0)
            .getTransactionId();

        // User B must not read, update, or delete User A's transaction
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.getTransactionFormForEdit(userB.getUserId(), userATxId)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.deleteTransaction(userB.getUserId(), userATxId)
        );
        assertTrue(transactionService.getFilteredTransactions(userB.getUserId(), null, null).isEmpty());
        assertEquals(
            0,
            transactionService.getDashboardSummary(userB.getUserId()).getTotalBalance().compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void calculatesDashboardBalanceFiltersAndAnalyticsFromMySqlRecords() {
        LocalDate today = LocalDate.now(IST);
        LocalDate lastMonth = today.minusMonths(1);
        String currentMonthStr = YearMonth.from(today).toString();

        // Current month income: ₹25,000.00
        createTx(userA.getUserId(), CategoryType.INCOME, salaryIncomeCategory.getCategoryId(),
            new BigDecimal("25000.00"), today, "Monthly Salary");
        // Current month expenses: ₹1,500.00 (Food) + ₹500.00 (Travel) = ₹2,000.00
        createTx(userA.getUserId(), CategoryType.EXPENSE, foodExpenseCategory.getCategoryId(),
            new BigDecimal("1500.00"), today, "Groceries");
        createTx(userA.getUserId(), CategoryType.EXPENSE, travelExpenseCategory.getCategoryId(),
            new BigDecimal("500.00"), today, "Metro");
        // Previous month expense: ₹1,000.00
        createTx(userA.getUserId(), CategoryType.EXPENSE, foodExpenseCategory.getCategoryId(),
            new BigDecimal("1000.00"), lastMonth, "Previous Month Meal");

        entityManager.flush();
        entityManager.clear();

        DashboardSummaryDto summary = transactionService.getDashboardSummary(userA.getUserId());
        assertEquals(0, new BigDecimal("25000.00").compareTo(summary.getTotalIncome()));
        assertEquals(0, new BigDecimal("3000.00").compareTo(summary.getTotalExpenses()));
        assertEquals(0, new BigDecimal("22000.00").compareTo(summary.getTotalBalance()));
        assertEquals(0, new BigDecimal("25000.00").compareTo(summary.getCurrentMonthIncome()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(summary.getCurrentMonthExpenses()));

        // Filter by current month & EXPENSE
        List<TransactionViewDto> filtered = transactionService.getFilteredTransactions(
            userA.getUserId(), CategoryType.EXPENSE, currentMonthStr
        );
        assertEquals(2, filtered.size());

        // Analytics for current month
        AnalyticsDto analytics = analyticsService.getAnalytics(
            userA.getUserId(), currentMonthStr, today.getYear()
        );
        assertEquals(0, new BigDecimal("25000.00").compareTo(analytics.getTotalIncome()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(analytics.getTotalExpense()));
        assertEquals(2, analytics.getCategoryBreakdown().size());
        assertEquals("Food", analytics.getCategoryBreakdown().get(0).getCategoryName());
        assertEquals(75.0, analytics.getCategoryBreakdown().get(0).getPercentage());

        // Empty user analytics check
        AnalyticsDto emptyAnalytics = analyticsService.getAnalytics(
            userB.getUserId(), currentMonthStr, today.getYear()
        );
        assertEquals(0, BigDecimal.ZERO.compareTo(emptyAnalytics.getTotalExpense()));
        assertTrue(emptyAnalytics.getCategoryBreakdown().isEmpty());
    }

    private void createTx(
        Long userId,
        CategoryType type,
        Long categoryId,
        BigDecimal amount,
        LocalDate date,
        String desc
    ) {
        TransactionForm form = new TransactionForm();
        form.setTransactionType(type);
        form.setCategoryId(categoryId);
        form.setAmount(amount);
        form.setTransactionDate(date);
        form.setDescription(desc);
        transactionService.createTransaction(userId, form);
    }

    private User createTestUser(String name) {
        RegisterRequest req = new RegisterRequest();
        req.setFullName(name);
        req.setEmail("tx_" + UUID.randomUUID() + "@paisatrack.in");
        req.setPassword("Password123");
        req.setConfirmPassword("Password123");
        return userService.registerUser(req);
    }
}
