package com.paisatrack.controller;

import com.paisatrack.dto.DashboardSummaryDto;
import com.paisatrack.security.CustomUserDetails;
import com.paisatrack.service.TransactionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the Home Dashboard showing total balance, income, expenses, and recent transactions.
 */
@Controller
public class DashboardController {

    private final TransactionService transactionService;

    public DashboardController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(
        @AuthenticationPrincipal CustomUserDetails principal,
        Model model
    ) {
        DashboardSummaryDto summary = transactionService.getDashboardSummary(principal.getUserId());
        model.addAttribute("summary", summary);
        model.addAttribute("userFullName", principal.getFullName());
        model.addAttribute("activeTab", "home");
        return "dashboard";
    }
}
