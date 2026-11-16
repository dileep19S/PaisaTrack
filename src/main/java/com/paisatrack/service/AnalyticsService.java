package com.paisatrack.service;

import com.paisatrack.dto.AnalyticsDto;

/**
 * Service interface for calculating MySQL-backed expense analytics and monthly comparisons.
 */
public interface AnalyticsService {

    AnalyticsDto getAnalytics(Long userId, String monthYear, Integer year);
}
