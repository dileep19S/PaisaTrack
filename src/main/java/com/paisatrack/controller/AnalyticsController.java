package com.paisatrack.controller;

import com.paisatrack.dto.AnalyticsDto;
import com.paisatrack.security.CustomUserDetails;
import com.paisatrack.service.AnalyticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Serves the Analytics view and its MySQL-calculated JSON data endpoint for Chart.js.
 */
@Controller
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public String showAnalyticsPage(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestParam(name = "month", required = false) String month,
        @RequestParam(name = "year", required = false) Integer year,
        Model model
    ) {
        AnalyticsDto analytics = analyticsService.getAnalytics(principal.getUserId(), month, year);
        model.addAttribute("analytics", analytics);
        model.addAttribute("activeTab", "analytics");
        return "analytics";
    }

    @GetMapping("/api/analytics/data")
    @ResponseBody
    public AnalyticsDto getAnalyticsData(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestParam(name = "month", required = false) String month,
        @RequestParam(name = "year", required = false) Integer year
    ) {
        return analyticsService.getAnalytics(principal.getUserId(), month, year);
    }
}
