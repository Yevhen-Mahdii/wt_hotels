package ua.edu.hotels.data.dto;

import ua.edu.hotels.data.entity.Revenue;

import java.math.BigDecimal;
import java.util.List;

/**
 * Returns gross revenue and its completed-stay records, without payment or expense accounting.
 */
public record RevenueSummary(BigDecimal total, List<Revenue> items) {
}
