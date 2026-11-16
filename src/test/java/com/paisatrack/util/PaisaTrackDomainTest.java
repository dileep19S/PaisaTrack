package com.paisatrack.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.paisatrack.entity.CategoryType;
import com.paisatrack.entity.ExpenseTransaction;
import com.paisatrack.entity.IncomeTransaction;
import com.paisatrack.entity.Transaction;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying Indian Rupee formatting and OOP polymorphic Transaction behavior.
 */
class PaisaTrackDomainTest {

    private final IndianCurrencyFormatter formatter = new IndianCurrencyFormatter();

    @Test
    void formatsIndianRupeesCorrectly() {
        assertEquals("₹250.00", formatter.format(new BigDecimal("250.00")));
        assertEquals("₹1,250.00", formatter.format(new BigDecimal("1250.00")));
        assertEquals("₹12,500.00", formatter.format(new BigDecimal("12500.00")));
        assertEquals("₹1,25,000.00", formatter.format(new BigDecimal("125000.00")));
        assertEquals("-₹1,250.00", formatter.format(new BigDecimal("-1250.00")));
    }

    @Test
    void demonstratesPolymorphicSignedAmountAndPrefix() {
        Transaction income = new IncomeTransaction();
        income.setAmount(new BigDecimal("15000.00"));

        Transaction expense = new ExpenseTransaction();
        expense.setAmount(new BigDecimal("450.00"));

        assertEquals(CategoryType.INCOME, income.getType());
        assertEquals(new BigDecimal("15000.00"), income.getSignedAmount());
        assertEquals("+", income.getFormattedPrefix());

        assertEquals(CategoryType.EXPENSE, expense.getType());
        assertEquals(new BigDecimal("-450.00"), expense.getSignedAmount());
        assertEquals("-", expense.getFormattedPrefix());
    }
}
