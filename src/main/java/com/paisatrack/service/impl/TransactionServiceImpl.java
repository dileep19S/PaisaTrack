package com.paisatrack.service.impl;

import com.paisatrack.dto.DashboardSummaryDto;
import com.paisatrack.dto.TransactionForm;
import com.paisatrack.dto.TransactionViewDto;
import com.paisatrack.entity.Category;
import com.paisatrack.entity.CategoryType;
import com.paisatrack.entity.ExpenseTransaction;
import com.paisatrack.entity.IncomeTransaction;
import com.paisatrack.entity.Transaction;
import com.paisatrack.entity.User;
import com.paisatrack.repository.CategoryRepository;
import com.paisatrack.repository.TransactionRepository;
import com.paisatrack.repository.UserRepository;
import com.paisatrack.service.TransactionService;
import com.paisatrack.util.IndianCurrencyFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements dashboard calculations and transaction CRUD operations in MySQL.
 * Demonstrates polymorphic instantiation of IncomeTransaction and ExpenseTransaction.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter MONTH_LABEL_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IndianCurrencyFormatter formatter;

    public TransactionServiceImpl(
        TransactionRepository transactionRepository,
        UserRepository userRepository,
        CategoryRepository categoryRepository,
        IndianCurrencyFormatter formatter
    ) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.formatter = formatter;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Long userId) {
        BigDecimal totalIncome = safeAmount(
            transactionRepository.sumAmountByUserAndType(userId, CategoryType.INCOME)
        );
        BigDecimal totalExpenses = safeAmount(
            transactionRepository.sumAmountByUserAndType(userId, CategoryType.EXPENSE)
        );
        BigDecimal totalBalance = totalIncome.subtract(totalExpenses);

        YearMonth currentMonth = YearMonth.now(IST_ZONE);
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        BigDecimal currentMonthIncome = safeAmount(
            transactionRepository.sumAmountByUserAndTypeBetween(
                userId, CategoryType.INCOME, monthStart, monthEnd
            )
        );
        BigDecimal currentMonthExpenses = safeAmount(
            transactionRepository.sumAmountByUserAndTypeBetween(
                userId, CategoryType.EXPENSE, monthStart, monthEnd
            )
        );

        List<TransactionViewDto> recent = transactionRepository
            .findRecentByUserId(userId, PageRequest.of(0, 6))
            .stream()
            .map(this::toViewDto)
            .toList();

        return new DashboardSummaryDto(
            totalBalance,
            totalIncome,
            totalExpenses,
            currentMonthIncome,
            currentMonthExpenses,
            currentMonth.format(MONTH_LABEL_FORMAT),
            recent
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionViewDto> getFilteredTransactions(Long userId, CategoryType type, String monthYear) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (monthYear != null && !monthYear.isBlank()) {
            try {
                YearMonth ym = YearMonth.parse(monthYear.trim());
                startDate = ym.atDay(1);
                endDate = ym.atEndOfMonth();
            } catch (DateTimeParseException ignored) {
                // Ignore invalid month string and return unfiltered dates
            }
        }

        return transactionRepository.findFilteredByUser(userId, type, startDate, endDate)
            .stream()
            .map(this::toViewDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionForm getTransactionFormForEdit(Long userId, Long transactionId) {
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        TransactionForm form = new TransactionForm();
        form.setTransactionId(tx.getTransactionId());
        form.setTransactionType(tx.getType());
        form.setCategoryId(tx.getCategory().getCategoryId());
        form.setAmount(tx.getAmount());
        form.setTransactionDate(tx.getTransactionDate());
        form.setDescription(tx.getDescription());
        return form;
    }

    @Override
    @Transactional
    public void createTransaction(Long userId, TransactionForm form) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
        Category category = validateCategoryForType(form.getCategoryId(), form.getTransactionType());

        // Polymorphic factory: instantiate concrete subclass based on transaction type
        Transaction tx = instantiateTransaction(form.getTransactionType());
        tx.setUser(user);
        tx.setCategory(category);
        tx.setAmount(normalizeAmount(form.getAmount()));
        tx.setTransactionDate(form.getTransactionDate());
        tx.setDescription(cleanDescription(form.getDescription()));

        transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public void updateTransaction(Long userId, Long transactionId, TransactionForm form) {
        Transaction existing = transactionRepository.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
        Category category = validateCategoryForType(form.getCategoryId(), form.getTransactionType());
        BigDecimal normalizedAmount = normalizeAmount(form.getAmount());

        if (existing.getType() == form.getTransactionType()) {
            existing.setCategory(category);
            existing.setAmount(normalizedAmount);
            existing.setTransactionDate(form.getTransactionDate());
            existing.setDescription(cleanDescription(form.getDescription()));
            transactionRepository.save(existing);
        } else {
            // Switch runtime subclass cleanly when changing INCOME <-> EXPENSE
            transactionRepository.delete(existing);
            transactionRepository.flush();

            Transaction replacement = instantiateTransaction(form.getTransactionType());
            replacement.setUser(userRepository.getReferenceById(userId));
            replacement.setCategory(category);
            replacement.setAmount(normalizedAmount);
            replacement.setTransactionDate(form.getTransactionDate());
            replacement.setDescription(cleanDescription(form.getDescription()));
            transactionRepository.save(replacement);
        }
    }

    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction existing = transactionRepository.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
        transactionRepository.delete(existing);
    }

    private Transaction instantiateTransaction(CategoryType type) {
        return (type == CategoryType.INCOME) ? new IncomeTransaction() : new ExpenseTransaction();
    }

    private Category validateCategoryForType(Long categoryId, CategoryType expectedType) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Selected category does not exist."));
        if (category.getCategoryType() != expectedType) {
            throw new IllegalArgumentException(
                "Selected category does not belong to " + expectedType.getLabel() + "."
            );
        }
        return category;
    }

    private TransactionViewDto toViewDto(Transaction tx) {
        // Polymorphic dispatch on tx: getType(), getSignedAmount(), getFormattedPrefix(), getAmountCssClass()
        CategoryType type = tx.getType();
        return new TransactionViewDto(
            tx.getTransactionId(),
            type,
            tx.getCategory().getCategoryId(),
            tx.getCategory().getCategoryName(),
            tx.getAmount(),
            tx.getSignedAmount(),
            formatter.format(tx.getAmount()),
            tx.getFormattedPrefix() + formatter.format(tx.getAmount()),
            tx.getTransactionDate(),
            formatter.formatDate(tx.getTransactionDate()),
            tx.getDescription(),
            tx.getAmountCssClass()
        );
    }

    private String cleanDescription(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Amount is required.");
        }
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        if (scaled.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than ₹0.00.");
        }
        return scaled;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
