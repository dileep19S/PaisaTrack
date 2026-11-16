package com.paisatrack.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payload returned by MySQL-backed analytics queries for Chart.js visualizations.
 */
public class AnalyticsDto {

    private final String selectedMonth;
    private final int selectedYear;
    private final BigDecimal totalIncome;
    private final BigDecimal totalExpense;
    private final BigDecimal netBalance;
    private final String formattedTotalIncome;
    private final String formattedTotalExpense;
    private final String formattedNetBalance;
    private final List<CategoryBreakdownItem> categoryBreakdown;
    private final List<MonthlyComparisonItem> monthlyComparison;
    private final List<Integer> availableYears;

    public AnalyticsDto(
        String selectedMonth,
        int selectedYear,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        String formattedTotalIncome,
        String formattedTotalExpense,
        String formattedNetBalance,
        List<CategoryBreakdownItem> categoryBreakdown,
        List<MonthlyComparisonItem> monthlyComparison,
        List<Integer> availableYears
    ) {
        this.selectedMonth = selectedMonth;
        this.selectedYear = selectedYear;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.formattedTotalIncome = formattedTotalIncome;
        this.formattedTotalExpense = formattedTotalExpense;
        this.formattedNetBalance = formattedNetBalance;
        this.categoryBreakdown = categoryBreakdown;
        this.monthlyComparison = monthlyComparison;
        this.availableYears = availableYears;
    }

    public String getSelectedMonth() {
        return selectedMonth;
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public String getFormattedTotalIncome() {
        return formattedTotalIncome;
    }

    public String getFormattedTotalExpense() {
        return formattedTotalExpense;
    }

    public String getFormattedNetBalance() {
        return formattedNetBalance;
    }

    public List<CategoryBreakdownItem> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public List<MonthlyComparisonItem> getMonthlyComparison() {
        return monthlyComparison;
    }

    public List<Integer> getAvailableYears() {
        return availableYears;
    }

    public static class CategoryBreakdownItem {
        private final String categoryName;
        private final BigDecimal totalAmount;
        private final double percentage;
        private final String formattedAmount;

        public CategoryBreakdownItem(
            String categoryName,
            BigDecimal totalAmount,
            double percentage,
            String formattedAmount
        ) {
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
            this.percentage = percentage;
            this.formattedAmount = formattedAmount;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public double getPercentage() {
            return percentage;
        }

        public String getFormattedAmount() {
            return formattedAmount;
        }
    }

    public static class MonthlyComparisonItem {
        private final int monthNumber;
        private final String monthLabel;
        private final BigDecimal income;
        private final BigDecimal expense;

        public MonthlyComparisonItem(
            int monthNumber,
            String monthLabel,
            BigDecimal income,
            BigDecimal expense
        ) {
            this.monthNumber = monthNumber;
            this.monthLabel = monthLabel;
            this.income = income;
            this.expense = expense;
        }

        public int getMonthNumber() {
            return monthNumber;
        }

        public String getMonthLabel() {
            return monthLabel;
        }

        public BigDecimal getIncome() {
            return income;
        }

        public BigDecimal getExpense() {
            return expense;
        }
    }
}
