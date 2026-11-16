package com.paisatrack.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

/**
 * Concrete subclass representing an EXPENSE transaction (e.g., Food, Travel, Bills).
 * Overrides polymorphic methods to subtract from balance.
 */
@Entity
@DiscriminatorValue("EXPENSE")
public class ExpenseTransaction extends Transaction {

    public ExpenseTransaction() {
        super();
    }

    @Override
    public CategoryType getType() {
        return CategoryType.EXPENSE;
    }

    @Override
    public BigDecimal getSignedAmount() {
        return getAmount() != null ? getAmount().negate() : BigDecimal.ZERO;
    }

    @Override
    public String getFormattedPrefix() {
        return "-";
    }

    @Override
    public String getAmountCssClass() {
        return "amount-expense";
    }
}
