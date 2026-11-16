package com.paisatrack.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated financial summary for the Home Dashboard.
 * Uses BigDecimal exclusively for all monetary totals.
 */
public class DashboardSummaryDto {

    private final BigDecimal totalBalance;
    private final BigDecimal totalIncome;
    private final BigDecimal totalExpenses;
    private final BigDecimal currentMonthIncome;
    private final BigDecimal currentMonthExpenses;
    private final String currentMonthLabel;
    private final List<TransactionViewDto> recentTransactions;

    public DashboardSummaryDto(
        BigDecimal totalBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal currentMonthIncome,
        BigDecimal currentMonthExpenses,
        String currentMonthLabel,
        List<TransactionViewDto> recentTransactions
    ) {
        this.totalBalance = totalBalance;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.currentMonthIncome = currentMonthIncome;
        this.currentMonthExpenses = currentMonthExpenses;
        this.currentMonthLabel = currentMonthLabel;
        this.recentTransactions = recentTransactions;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public BigDecimal getCurrentMonthIncome() {
        return currentMonthIncome;
    }

    public BigDecimal getCurrentMonthExpenses() {
        return currentMonthExpenses;
    }

    public String getCurrentMonthLabel() {
        return currentMonthLabel;
    }

    public List<TransactionViewDto> getRecentTransactions() {
        return recentTransactions;
    }
}
