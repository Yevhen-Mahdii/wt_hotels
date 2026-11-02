package ua.edu.hotels.data.dto;

import java.math.BigDecimal;
import java.util.List;
import ua.edu.hotels.data.entity.*;

/** Returns gross revenue and its completed-stay records, without payment or expense accounting. */
public record RevenueSummary(BigDecimal total, List<Revenue> items) {}
