package com.paisatrack.service;

import com.paisatrack.dto.DashboardSummaryDto;
import com.paisatrack.dto.TransactionForm;
import com.paisatrack.dto.TransactionViewDto;
import com.paisatrack.entity.CategoryType;
import java.util.List;

/**
 * Service interface for dashboard summaries and user-isolated transaction CRUD.
 */
public interface TransactionService {

    DashboardSummaryDto getDashboardSummary(Long userId);

    List<TransactionViewDto> getFilteredTransactions(Long userId, CategoryType type, String monthYear);

    TransactionForm getTransactionFormForEdit(Long userId, Long transactionId);

    void createTransaction(Long userId, TransactionForm form);

    void updateTransaction(Long userId, Long transactionId, TransactionForm form);

    void deleteTransaction(Long userId, Long transactionId);
}
