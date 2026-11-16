package com.paisatrack.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

/**
 * Concrete subclass representing an INCOME transaction (e.g., Salary, Pocket Money, Freelance).
 * Overrides polymorphic methods to add positively to balance.
 */
@Entity
@DiscriminatorValue("INCOME")
public class IncomeTransaction extends Transaction {

    public IncomeTransaction() {
        super();
    }

    @Override
    public CategoryType getType() {
        return CategoryType.INCOME;
    }

    @Override
    public BigDecimal getSignedAmount() {
        return getAmount() != null ? getAmount() : BigDecimal.ZERO;
    }

    @Override
    public String getFormattedPrefix() {
        return "+";
    }

    @Override
    public String getAmountCssClass() {
        return "amount-income";
    }
}
