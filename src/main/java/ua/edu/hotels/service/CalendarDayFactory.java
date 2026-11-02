package ua.edu.hotels.service;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.edu.hotels.data.dto.CalendarDay;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;

/** Combines planned reservations and actual occupancy without hiding conflicting states. */
@Component
@RequiredArgsConstructor
public class CalendarDayFactory {
  private final StayCalculator stayCalculator;

  public CalendarDay create(
      Room room, LocalDate date, LocalDate today, List<Booking> bookings, List<Stay> stays) {
    List<String> states = new ArrayList<>();
    List<Long> bookingIds = new ArrayList<>();
    for (Booking booking : bookings) {
      if (!date.isBefore(booking.getStartDate()) && date.isBefore(booking.getEndDate())) {
        states.add(BookingStatus.RESERVED.name());
        bookingIds.add(booking.getId());
      }
    }
    if (stays.stream().anyMatch(stay -> occupies(stay, date, today)))
      states.add(RoomState.OCCUPIED.name());
    if (date.equals(today) && room.getState() == RoomState.CLEANING)
      states.add(RoomState.CLEANING.name());
    if (states.isEmpty()) states.add(RoomState.FREE.name());
    return new CalendarDay(date, List.copyOf(states), states.size() > 1, List.copyOf(bookingIds));
  }

  private boolean occupies(Stay stay, LocalDate date, LocalDate today) {
    LocalDate first = stayCalculator.accountingDay(stay.getCheckedInAt());
    LocalDate end =
        stay.getCheckedOutAt() == null
            ? today.plusDays(1)
            : stayCalculator.accountingDay(stay.getCheckedOutAt());
    if (!end.isAfter(first)) end = first.plusDays(1);
    return !date.isBefore(first) && date.isBefore(end);
  }
}
