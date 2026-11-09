package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Validates half-open date ranges; booking dates must not start in the past.
 */
@Component
@RequiredArgsConstructor
public class DateRangePolicy {
  public static final long MAX_DAYS = 366;
  private final Clock clock;

  public void validate(LocalDate start, LocalDate end) {
    if (start == null
      || end == null
      || !end.isAfter(start)
      || ChronoUnit.DAYS.between(start, end) > MAX_DAYS)
      throw new ResponseStatusException(BAD_REQUEST, "Оберіть період від 1 до 366 ночей");
  }

  public void validateBooking(LocalDate start, LocalDate end) {
    validate(start, end);
    if (start.isBefore(LocalDate.now(clock)))
      throw new ResponseStatusException(BAD_REQUEST, "Не можна бронювати минулі дати");
  }
}
