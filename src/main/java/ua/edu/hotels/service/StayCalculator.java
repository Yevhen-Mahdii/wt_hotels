package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Calculates accounting days in the configured time zone using noon as the day boundary.
 */
@Component
@RequiredArgsConstructor
public class StayCalculator {
  private final Clock clock;

  public LocalDate accountingDay(Instant instant) {
    var local = instant.atZone(clock.getZone());
    return local.toLocalTime().isBefore(LocalTime.NOON)
      ? local.toLocalDate().minusDays(1)
      : local.toLocalDate();
  }

  public Instant boundary(LocalDate date) {
    return date.atTime(LocalTime.NOON).atZone(clock.getZone()).toInstant();
  }

  public long nights(Instant start, Instant end) {
    if (end.isBefore(start)) throw new IllegalArgumentException("Checkout precedes check-in");
    // Count local accounting dates rather than 24-hour durations, including daylight-saving changes.
    return Math.max(1, ChronoUnit.DAYS.between(accountingDay(start), accountingDay(end)));
  }
}
