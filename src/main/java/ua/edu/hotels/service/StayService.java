package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.repository.*;
import ua.edu.hotels.security.HotelToken;

/** Transitions rooms through occupancy and cleaning, recording revenue atomically at checkout. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StayService {
  private final RoomService roomService;
  private final StayRepository stayRepository;
  private final BookingRepository bookingRepository;
  private final BookingService bookingService;
  private final RevenueRepository revenueRepository;
  private final AccessPolicy accessPolicy;
  private final StayCalculator stayCalculator;
  private final Clock clock;

  @Transactional
  public Stay checkIn(HotelToken actor, Long roomId, Long bookingId) {
    accessPolicy.requireRole(actor, Role.MANAGER);
    Room room = roomService.lock(roomId);
    accessPolicy.requireHotelAccess(actor, room.getHotelId());
    if (room.getState() != RoomState.FREE)
      throw new ResponseStatusException(CONFLICT, "Спочатку звільніть номер і завершіть клінінг");
    LocalDate today = LocalDate.now(clock);
    if (bookingId != null) {
      Booking booking = bookingRepository.findById(bookingId).orElseThrow(ServiceErrors::notFound);
      if (!booking.getRoomId().equals(roomId)
          || booking.getStatus() != BookingStatus.RESERVED
          || today.isBefore(booking.getStartDate())
          || !today.isBefore(booking.getEndDate()))
        throw new ResponseStatusException(
            CONFLICT, "Бронювання не відповідає номеру або даті заселення");
      booking.setStatus(BookingStatus.CHECKED_IN);
    } else if (bookingService.overlaps(roomId, today, today.plusDays(1), null)) {
      throw new ResponseStatusException(
          CONFLICT, "На сьогодні є бронювання: оберіть заселення за ним");
    }
    Stay stay = new Stay();
    stay.setRoomId(roomId);
    stay.setBookingId(bookingId);
    stay.setCheckedInAt(clock.instant());
    // Snapshot the nightly rate; later room-price edits must not change this stay.
    stay.setPrice(room.getPrice());
    room.setState(RoomState.OCCUPIED);
    return stayRepository.save(stay);
  }

  @Transactional
  public Revenue checkOut(HotelToken actor, Long roomId) {
    accessPolicy.requireRole(actor, Role.MANAGER);
    Room room = roomService.lock(roomId);
    accessPolicy.requireHotelAccess(actor, room.getHotelId());
    if (room.getState() != RoomState.OCCUPIED)
      throw new ResponseStatusException(CONFLICT, "Номер не зайнятий");
    Stay stay =
        stayRepository
            .findByRoomIdAndCheckedOutAtIsNull(roomId)
            .orElseThrow(ServiceErrors::notFound);
    stay.setCheckedOutAt(clock.instant());
    // Stay completion, room cleaning state and revenue commit together or roll back together.
    Revenue income = new Revenue();
    income.setStayId(stay.getId());
    income.setHotelId(room.getHotelId());
    income.setRecordedAt(stay.getCheckedOutAt());
    income.setNights(stayCalculator.nights(stay.getCheckedInAt(), stay.getCheckedOutAt()));
    income.setAmount(stay.getPrice().multiply(BigDecimal.valueOf(income.getNights())));
    room.setState(RoomState.CLEANING);
    if (stay.getBookingId() != null)
      bookingRepository
          .findById(stay.getBookingId())
          .orElseThrow(ServiceErrors::notFound)
          .setStatus(BookingStatus.COMPLETED);
    return revenueRepository.save(income);
  }

  @Transactional
  public Room completeCleaning(HotelToken actor, Long roomId) {
    accessPolicy.requireRole(actor, Role.MANAGER);
    Room room = roomService.lock(roomId);
    accessPolicy.requireHotelAccess(actor, room.getHotelId());
    if (room.getState() != RoomState.CLEANING)
      throw new ResponseStatusException(CONFLICT, "Номер не потребує клінінгу");
    room.setState(RoomState.FREE);
    return room;
  }
}
