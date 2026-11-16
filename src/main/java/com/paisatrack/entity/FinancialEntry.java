package com.paisatrack.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstraction representing any financial ledger entry in PaisaTrack.
 * Implemented by the abstract Transaction base class and concretely
 * realized through polymorphic IncomeTransaction and ExpenseTransaction subclasses.
 */
public interface FinancialEntry {

    BigDecimal getAmount();

    /**
     * Returns +amount for income or -amount for expense.
     */
    BigDecimal getSignedAmount();

    CategoryType getType();

    String getFormattedPrefix();

    LocalDate getTransactionDate();
}
