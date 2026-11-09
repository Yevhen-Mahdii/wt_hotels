package ua.edu.hotels.data.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Preserves all applicable states for a date so overlapping plans and actual state remain
 * visible.
 */
public record CalendarDay(
  LocalDate date, List<String> states, boolean conflict, List<Long> bookingIds) {
}
