package ua.edu.hotels.service;

import static org.assertj.core.api.Assertions.*;
import static ua.edu.hotels.support.Fixtures.*;

import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import ua.edu.hotels.data.enums.*;

/** Checks visible calendar conflicts between reservations and actual room state. */
class CalendarDayFactoryTest {
  private final CalendarDayFactory calendarDayFactory =
      new CalendarDayFactory(new StayCalculator(CLOCK));
  private final LocalDate today = LocalDate.now(CLOCK);

  @Test
  void displaysReservationAndCleaningTogether() {
    var day =
        calendarDayFactory.create(
            room(RoomState.CLEANING), today, today, List.of(booking()), List.of());
    assertThat(day.states()).containsExactly("RESERVED", "CLEANING");
    assertThat(day.conflict()).isTrue();
  }

  @Test
  void displaysReservationAndActualOccupancyTogether() {
    var day =
        calendarDayFactory.create(
            room(RoomState.OCCUPIED), today, today, List.of(booking()), List.of(stay()));
    assertThat(day.states()).containsExactly("RESERVED", "OCCUPIED");
    assertThat(day.conflict()).isTrue();
  }

  @Test
  void endDateDoesNotReserveFollowingNight() {
    var day =
        calendarDayFactory.create(
            room(RoomState.FREE), booking().getEndDate(), today, List.of(booking()), List.of());
    assertThat(day.states()).containsExactly("FREE");
    assertThat(day.conflict()).isFalse();
  }

  @Test
  void currentCleaningDoesNotBlockFutureDays() {
    var day =
        calendarDayFactory.create(
            room(RoomState.CLEANING), today.plusDays(1), today, List.of(), List.of());
    assertThat(day.states()).containsExactly("FREE");
  }
}
