package com.paisatrack.controller;

import com.paisatrack.dto.TransactionForm;
import com.paisatrack.dto.TransactionViewDto;
import com.paisatrack.entity.CategoryType;
import com.paisatrack.security.CustomUserDetails;
import com.paisatrack.service.CategoryService;
import com.paisatrack.service.TransactionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles Transaction History filtering, Add Transaction, Edit Transaction, and Delete operations.
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public TransactionController(
        TransactionService transactionService,
        CategoryService categoryService
    ) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String showHistory(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestParam(name = "type", required = false, defaultValue = "ALL") String typeParam,
        @RequestParam(name = "month", required = false) String monthParam,
        Model model
    ) {
        CategoryType filterType = parseTypeOrNull(typeParam);
        String cleanMonth = (monthParam != null && !monthParam.isBlank()) ? monthParam.trim() : "";

        List<TransactionViewDto> transactions = transactionService.getFilteredTransactions(
            principal.getUserId(),
            filterType,
            cleanMonth
        );

        BigDecimal filteredIncome = transactions.stream()
            .filter(t -> t.getType() == CategoryType.INCOME)
            .map(TransactionViewDto::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal filteredExpense = transactions.stream()
            .filter(t -> t.getType() == CategoryType.EXPENSE)
            .map(TransactionViewDto::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("transactions", transactions);
        model.addAttribute("selectedType", filterType == null ? "ALL" : filterType.name());
        model.addAttribute("selectedMonth", cleanMonth);
        model.addAttribute("filteredIncome", filteredIncome);
        model.addAttribute("filteredExpense", filteredExpense);
        model.addAttribute("activeTab", "history");
        return "transactions/history";
    }

    @GetMapping("/new")
    public String showAddForm(
        @RequestParam(name = "type", required = false, defaultValue = "EXPENSE") String typeParam,
        Model model
    ) {
        CategoryType defaultType = parseTypeOrNull(typeParam);
        if (defaultType == null) {
            defaultType = CategoryType.EXPENSE;
        }

        TransactionForm form = new TransactionForm();
        form.setTransactionType(defaultType);
        form.setTransactionDate(LocalDate.now(IST_ZONE));

        var defaultCategories = categoryService.getCategoriesByType(defaultType);
        if (!defaultCategories.isEmpty()) {
            form.setCategoryId(defaultCategories.get(0).getCategoryId());
        }

        populateFormCategories(model);
        model.addAttribute("transactionForm", form);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeTab", "history");
        return "transactions/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable("id") Long transactionId,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        try {
            TransactionForm form = transactionService.getTransactionFormForEdit(
                principal.getUserId(),
                transactionId
            );
            populateFormCategories(model);
            model.addAttribute("transactionForm", form);
            model.addAttribute("isEdit", true);
            model.addAttribute("activeTab", "history");
            return "transactions/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/transactions";
        }
    }

    @PostMapping("/save")
    public String saveTransaction(
        @AuthenticationPrincipal CustomUserDetails principal,
        @Valid @ModelAttribute("transactionForm") TransactionForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        boolean isEdit = form.getTransactionId() != null;
        if (bindingResult.hasErrors()) {
            populateFormCategories(model);
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("activeTab", "history");
            return "transactions/form";
        }

        try {
            if (isEdit) {
                transactionService.updateTransaction(
                    principal.getUserId(),
                    form.getTransactionId(),
                    form
                );
                redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transaction updated successfully."
                );
            } else {
                transactionService.createTransaction(principal.getUserId(), form);
                redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transaction added successfully."
                );
            }
            return "redirect:/transactions";
        } catch (IllegalArgumentException ex) {
            populateFormCategories(model);
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("formError", ex.getMessage());
            model.addAttribute("activeTab", "history");
            return "transactions/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable("id") Long transactionId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            transactionService.deleteTransaction(principal.getUserId(), transactionId);
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Transaction deleted."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/transactions";
    }

    private void populateFormCategories(Model model) {
        model.addAttribute(
            "expenseCategories",
            categoryService.getCategoriesByType(CategoryType.EXPENSE)
        );
        model.addAttribute(
            "incomeCategories",
            categoryService.getCategoriesByType(CategoryType.INCOME)
        );
    }

    private CategoryType parseTypeOrNull(String raw) {
        if (raw == null || raw.isBlank() || "ALL".equalsIgnoreCase(raw)) {
            return null;
        }
        try {
            return CategoryType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
