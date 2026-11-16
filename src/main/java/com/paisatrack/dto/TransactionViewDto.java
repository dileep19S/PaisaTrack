package com.paisatrack.dto;

import com.paisatrack.entity.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Presentation DTO for safely rendering transaction rows in Thymeleaf views.
 */
public class TransactionViewDto {

    private final Long transactionId;
    private final CategoryType type;
    private final Long categoryId;
    private final String categoryName;
    private final BigDecimal amount;
    private final BigDecimal signedAmount;
    private final String formattedAmount;
    private final String formattedSignedAmount;
    private final LocalDate transactionDate;
    private final String formattedDate;
    private final String description;
    private final String amountCssClass;

    public TransactionViewDto(
        Long transactionId,
        CategoryType type,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        BigDecimal signedAmount,
        String formattedAmount,
        String formattedSignedAmount,
        LocalDate transactionDate,
        String formattedDate,
        String description,
        String amountCssClass
    ) {
        this.transactionId = transactionId;
        this.type = type;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
        this.signedAmount = signedAmount;
        this.formattedAmount = formattedAmount;
        this.formattedSignedAmount = formattedSignedAmount;
        this.transactionDate = transactionDate;
        this.formattedDate = formattedDate;
        this.description = description;
        this.amountCssClass = amountCssClass;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public CategoryType getType() {
        return type;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getSignedAmount() {
        return signedAmount;
    }

    public String getFormattedAmount() {
        return formattedAmount;
    }

    public String getFormattedSignedAmount() {
        return formattedSignedAmount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getDescription() {
        return description;
    }

    public String getAmountCssClass() {
        return amountCssClass;
    }
}
