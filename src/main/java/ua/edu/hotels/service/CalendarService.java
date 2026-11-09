package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.data.dto.CalendarRow;
import ua.edu.hotels.data.entity.Booking;
import ua.edu.hotels.data.entity.Room;
import ua.edu.hotels.data.entity.Stay;
import ua.edu.hotels.data.enums.BookingStatus;
import ua.edu.hotels.repository.BookingRepository;
import ua.edu.hotels.repository.StayRepository;
import ua.edu.hotels.security.HotelToken;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Builds hotel-scoped calendar rows using batched booking and stay queries.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {
  private final RoomService roomService;
  private final BookingRepository bookingRepository;
  private final StayRepository stayRepository;
  private final DateRangePolicy dateRangePolicy;
  private final StayCalculator stayCalculator;
  private final CalendarDayFactory calendarDayFactory;
  private final Clock clock;

  public List<CalendarRow> calendar(
    HotelToken actor, LocalDate start, LocalDate end, Long hotelId) {
    dateRangePolicy.validate(start, end);
    var selectedRooms = roomService.list(actor, hotelId);
    if (selectedRooms.isEmpty()) return List.of();
    var ids = selectedRooms.stream().map(Room::getId).toList();
    // Fetch each collection once, rather than executing two queries for every room.
    var bookingsByRoom =
      bookingRepository.findForCalendar(ids, BookingStatus.RESERVED, start, end).stream()
        .collect(java.util.stream.Collectors.groupingBy(Booking::getRoomId));
    var staysByRoom =
      stayRepository
        .findForCalendar(ids, stayCalculator.boundary(start), stayCalculator.boundary(end))
        .stream()
        .collect(java.util.stream.Collectors.groupingBy(Stay::getRoomId));
    LocalDate today = LocalDate.now(clock);
    return selectedRooms.stream()
      .map(
        room ->
          new CalendarRow(
            room,
            start
              .datesUntil(end)
              .map(
                date ->
                  calendarDayFactory.create(
                    room,
                    date,
                    today,
                    bookingsByRoom.getOrDefault(room.getId(), List.of()),
                    staysByRoom.getOrDefault(room.getId(), List.of())))
              .toList()))
      .toList();
  }
}
