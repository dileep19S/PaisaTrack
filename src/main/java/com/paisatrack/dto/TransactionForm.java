package com.paisatrack.dto;

import com.paisatrack.entity.CategoryType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Form DTO for creating or editing a transaction.
 */
public class TransactionForm {

    private Long transactionId;

    @NotNull(message = "Please select Income or Expense")
    private CategoryType transactionType = CategoryType.EXPENSE;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least ₹0.01")
    @DecimalMax(value = "9999999999.99", message = "Amount exceeds maximum allowed limit")
    @Digits(integer = 10, fraction = 2, message = "Amount supports up to 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Please select a category")
    private Long categoryId;

    @NotNull(message = "Transaction date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public CategoryType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(CategoryType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
