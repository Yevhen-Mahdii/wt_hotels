package ua.edu.hotels.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static ua.edu.hotels.support.Fixtures.CLOCK;

import java.time.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Checks noon boundaries and minimum-night accounting using a deterministic clock.
 */
@ExtendWith(MockitoExtension.class)
class StayCalculatorTest {
  private final StayCalculator stayCalculator = new StayCalculator(CLOCK);

  @ParameterizedTest
  @CsvSource({
    "2026-11-03T09:59:59Z,2026-11-02",
    "2026-11-03T10:00:00Z,2026-11-03",
    "2026-07-03T08:59:59Z,2026-07-02",
    "2026-07-03T09:00:00Z,2026-07-03"
  })
  void accountingDateUsesKyivNoon(String instant, String expected) {
    assertThat(stayCalculator.accountingDay(Instant.parse(instant)))
      .isEqualTo(LocalDate.parse(expected));
  }

  @ParameterizedTest
  @CsvSource({
    "2026-11-02T10:00:00Z,2026-11-05T10:00:00Z,3",
    "2026-11-02T10:00:00Z,2026-11-02T11:00:00Z,1",
    "2026-11-02T10:00:00Z,2026-11-03T09:59:59Z,1",
    "2026-10-24T09:00:00Z,2026-10-25T10:00:00Z,1",
    "2026-03-28T10:00:00Z,2026-03-29T09:00:00Z,1"
  })
  void calculatesActualNightsAcrossClockChanges(String start, String end, long expected) {
    assertThat(stayCalculator.nights(Instant.parse(start), Instant.parse(end))).isEqualTo(expected);
  }

  @Test
  void rejectsCheckoutBeforeCheckIn() {
    assertThatThrownBy(
      () -> stayCalculator.nights(CLOCK.instant(), CLOCK.instant().minusSeconds(1)))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
