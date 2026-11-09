package ua.edu.hotels.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static ua.edu.hotels.support.Fixtures.*;

import java.time.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import ua.edu.hotels.data.enums.*;

/**
 * Checks visible calendar conflicts between reservations and actual room state.
 */
@ExtendWith(MockitoExtension.class)
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
  void sameDayCheckoutAndCleaningReleaseToday() {
    var completed = stay();
    completed.setCheckedInAt(CLOCK.instant());
    completed.setCheckedOutAt(CLOCK.instant().plusSeconds(3600));

    var cleaning = calendarDayFactory.create(
      room(RoomState.CLEANING), today, today, List.of(), List.of(completed));
    assertThat(cleaning.states()).containsExactly("CLEANING");
    assertThat(cleaning.conflict()).isFalse();

    var free = calendarDayFactory.create(
      room(RoomState.FREE), today, today, List.of(), List.of(completed));
    assertThat(free.states()).containsExactly("FREE");
    assertThat(free.conflict()).isFalse();

    var reserved = calendarDayFactory.create(
      room(RoomState.FREE), today, today, List.of(booking()), List.of(completed));
    assertThat(reserved.states()).containsExactly("RESERVED");
    assertThat(reserved.conflict()).isFalse();
  }

  @Test
  void currentOccupancyDoesNotDependOnCalendarStayQuery() {
    var day = calendarDayFactory.create(
      room(RoomState.OCCUPIED), today, today, List.of(), List.of());
    assertThat(day.states()).containsExactly("OCCUPIED");
  }

  @Test
  void completedSameDayStayRemainsVisibleInHistory() {
    var completed = stay();
    completed.setCheckedInAt(CLOCK.instant().minusSeconds(86400));
    completed.setCheckedOutAt(completed.getCheckedInAt().plusSeconds(3600));

    var day = calendarDayFactory.create(
      room(RoomState.FREE), today.minusDays(1), today, List.of(), List.of(completed));
    assertThat(day.states()).containsExactly("OCCUPIED");
  }

  @Test
  void currentCleaningDoesNotBlockFutureDays() {
    var day =
      calendarDayFactory.create(
        room(RoomState.CLEANING), today.plusDays(1), today, List.of(), List.of());
    assertThat(day.states()).containsExactly("FREE");
  }
}
