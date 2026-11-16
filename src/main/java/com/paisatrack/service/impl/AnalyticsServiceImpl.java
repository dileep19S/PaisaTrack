package com.paisatrack.service.impl;

import com.paisatrack.dto.AnalyticsDto;
import com.paisatrack.dto.AnalyticsDto.CategoryBreakdownItem;
import com.paisatrack.dto.AnalyticsDto.MonthlyComparisonItem;
import com.paisatrack.entity.CategoryType;
import com.paisatrack.repository.TransactionRepository;
import com.paisatrack.service.AnalyticsService;
import com.paisatrack.util.IndianCurrencyFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calculates real-time expense analytics and monthly comparisons directly from MySQL records.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private final TransactionRepository transactionRepository;
    private final IndianCurrencyFormatter formatter;

    public AnalyticsServiceImpl(
        TransactionRepository transactionRepository,
        IndianCurrencyFormatter formatter
    ) {
        this.transactionRepository = transactionRepository;
        this.formatter = formatter;
    }

    @Override
    public AnalyticsDto getAnalytics(Long userId, String monthYear, Integer year) {
        LocalDate today = LocalDate.now(IST_ZONE);
        int currentYear = today.getYear();

        String normalizedMonth = (monthYear == null || monthYear.isBlank())
            ? YearMonth.from(today).toString()
            : monthYear.trim();

        LocalDate startDate = null;
        LocalDate endDate = null;
        int selectedYear = (year != null) ? year : currentYear;

        if (!"ALL".equalsIgnoreCase(normalizedMonth)) {
            try {
                YearMonth ym = YearMonth.parse(normalizedMonth);
                startDate = ym.atDay(1);
                endDate = ym.atEndOfMonth();
                if (year == null) {
                    selectedYear = ym.getYear();
                }
            } catch (DateTimeParseException ex) {
                YearMonth fallback = YearMonth.from(today);
                normalizedMonth = fallback.toString();
                startDate = fallback.atDay(1);
                endDate = fallback.atEndOfMonth();
            }
        } else {
            normalizedMonth = "ALL";
        }

        BigDecimal totalIncome;
        BigDecimal totalExpense;
        if (startDate != null) {
            totalIncome = safeAmount(
                transactionRepository.sumAmountByUserAndTypeBetween(
                    userId, CategoryType.INCOME, startDate, endDate
                )
            );
            totalExpense = safeAmount(
                transactionRepository.sumAmountByUserAndTypeBetween(
                    userId, CategoryType.EXPENSE, startDate, endDate
                )
            );
        } else {
            totalIncome = safeAmount(
                transactionRepository.sumAmountByUserAndType(userId, CategoryType.INCOME)
            );
            totalExpense = safeAmount(
                transactionRepository.sumAmountByUserAndType(userId, CategoryType.EXPENSE)
            );
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        // Category-wise expense breakdown
        List<Object[]> rawCategories = transactionRepository.sumByCategoryForUser(
            userId, CategoryType.EXPENSE, startDate, endDate
        );
        List<CategoryBreakdownItem> categoryBreakdown = new ArrayList<>();
        for (Object[] row : rawCategories) {
            String categoryName = (String) row[0];
            BigDecimal catAmount = safeAmount((BigDecimal) row[1]);
            double percentage = 0.0;
            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                percentage = catAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalExpense, 1, RoundingMode.HALF_UP)
                    .doubleValue();
            }
            categoryBreakdown.add(new CategoryBreakdownItem(
                categoryName,
                catAmount,
                percentage,
                formatter.format(catAmount)
            ));
        }

        // Monthly Income vs Expense comparison for the selected year (Jan - Dec)
        BigDecimal[] monthlyIncome = new BigDecimal[12];
        BigDecimal[] monthlyExpense = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            monthlyIncome[i] = BigDecimal.ZERO;
            monthlyExpense[i] = BigDecimal.ZERO;
        }

        List<Object[]> rawMonthly = transactionRepository.findMonthlyTotalsByYear(userId, selectedYear);
        for (Object[] row : rawMonthly) {
            int monthIdx = ((Number) row[0]).intValue() - 1;
            CategoryType type = (CategoryType) row[1];
            BigDecimal sum = safeAmount((BigDecimal) row[2]);
            if (monthIdx >= 0 && monthIdx < 12) {
                if (type == CategoryType.INCOME) {
                    monthlyIncome[monthIdx] = sum;
                } else if (type == CategoryType.EXPENSE) {
                    monthlyExpense[monthIdx] = sum;
                }
            }
        }

        List<MonthlyComparisonItem> monthlyComparison = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            String label = Month.of(i + 1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthlyComparison.add(new MonthlyComparisonItem(
                i + 1,
                label,
                monthlyIncome[i],
                monthlyExpense[i]
            ));
        }

        Set<Integer> yearSet = new LinkedHashSet<>();
        yearSet.add(currentYear);
        yearSet.add(selectedYear);
        yearSet.addAll(transactionRepository.findDistinctYearsByUserId(userId));
        List<Integer> availableYears = yearSet.stream()
            .sorted((a, b) -> Integer.compare(b, a))
            .toList();

        return new AnalyticsDto(
            normalizedMonth,
            selectedYear,
            totalIncome,
            totalExpense,
            netBalance,
            formatter.format(totalIncome),
            formatter.format(totalExpense),
            formatter.format(netBalance),
            categoryBreakdown,
            monthlyComparison,
            availableYears
        );
    }

    private BigDecimal safeAmount(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}
