package ua.edu.hotels.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static ua.edu.hotels.support.Fixtures.CLOCK;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/** Checks date ordering, range limits and rejection of past booking dates. */
@ExtendWith(MockitoExtension.class)
class DateRangePolicyTest {
  private final DateRangePolicy dateRangePolicy = new DateRangePolicy(CLOCK);
  private final LocalDate today = LocalDate.now(CLOCK);

  @Test
  void acceptsTodayAndMaximumRange() {
    assertThatCode(() -> dateRangePolicy.validateBooking(today, today.plusDays(366)))
        .doesNotThrowAnyException();
  }

  @Test
  void rejectsEmptyRange() {
    assertThatThrownBy(() -> dateRangePolicy.validate(today, today))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void rejectsReversedRange() {
    assertThatThrownBy(() -> dateRangePolicy.validate(today, today.minusDays(1)))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void rejectsTooLongRange() {
    assertThatThrownBy(() -> dateRangePolicy.validate(today, today.plusDays(367)))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void rejectsMissingDate() {
    assertThatThrownBy(() -> dateRangePolicy.validate(null, today))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void rejectsBookingInPast() {
    assertThatThrownBy(() -> dateRangePolicy.validateBooking(today.minusDays(1), today))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void allowsHistoricalReport() {
    assertThatCode(() -> dateRangePolicy.validate(today.minusDays(10), today))
        .doesNotThrowAnyException();
  }
}
