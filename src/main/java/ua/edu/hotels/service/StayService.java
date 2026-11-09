package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.entity.Booking;
import ua.edu.hotels.data.entity.Revenue;
import ua.edu.hotels.data.entity.Room;
import ua.edu.hotels.data.entity.Stay;
import ua.edu.hotels.data.enums.BookingStatus;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.data.enums.RoomState;
import ua.edu.hotels.repository.BookingRepository;
import ua.edu.hotels.repository.RevenueRepository;
import ua.edu.hotels.repository.StayRepository;
import ua.edu.hotels.security.HotelToken;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * Transitions rooms through occupancy and cleaning, recording revenue atomically at checkout.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StayService {
  private final RoomService roomService;
  private final StayRepository stayRepository;
  private final BookingRepository bookingRepository;
  private final BookingService bookingService;
  private final RevenueRepository revenueRepository;
  private final StayCalculator stayCalculator;
  private final Clock clock;

  @Transactional
  public Stay checkIn(HotelToken actor, Long roomId, Long bookingId) {
    actor.requireRole(Role.MANAGER);
    Room room = roomService.lock(roomId);
    actor.requireHotelAccess(room.getHotelId());
    if (room.getState() != RoomState.FREE) {
      log.warn("Check-in rejected: room is not free; roomId={}, state={}", roomId, room.getState());
      throw new ResponseStatusException(CONFLICT, "Спочатку звільніть номер і завершіть клінінг");
    }
    LocalDate today = LocalDate.now(clock);
    if (bookingId != null) {
      Booking booking = bookingRepository.findById(bookingId).orElseThrow(ServiceErrors::notFound);
      if (!booking.getRoomId().equals(roomId)
        || booking.getStatus() != BookingStatus.RESERVED
        || today.isBefore(booking.getStartDate())
        || !today.isBefore(booking.getEndDate())) {
        log.warn("Check-in rejected: booking does not match room, status or date; roomId={}, bookingId={}",
          roomId, bookingId);
        throw new ResponseStatusException(
          CONFLICT, "Бронювання не відповідає номеру або даті заселення");
      }
      booking.setStatus(BookingStatus.CHECKED_IN);
    } else if (bookingService.overlaps(roomId, today, today.plusDays(1), null)) {
      log.warn("Walk-in rejected: room has an active booking today; roomId={}", roomId);
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
    Stay saved = stayRepository.save(stay);
    TransactionLog.info(log, "Check-in completed: stayId={}, roomId={}, bookingId={}, actorId={}",
      saved.getId(), roomId, bookingId, actor.getUserId());
    return saved;
  }

  @Transactional
  public Revenue checkOut(HotelToken actor, Long roomId) {
    actor.requireRole(Role.MANAGER);
    Room room = roomService.lock(roomId);
    actor.requireHotelAccess(room.getHotelId());
    if (room.getState() != RoomState.OCCUPIED) {
      log.warn("Check-out rejected: room is not occupied; roomId={}, state={}", roomId, room.getState());
      throw new ResponseStatusException(CONFLICT, "Номер не зайнятий");
    }
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
    Revenue saved = revenueRepository.save(income);
    TransactionLog.info(log, "Check-out completed: stayId={}, roomId={}, revenueId={}, actorId={}",
      stay.getId(), roomId, saved.getId(), actor.getUserId());
    return saved;
  }

  @Transactional
  public Room completeCleaning(HotelToken actor, Long roomId) {
    actor.requireRole(Role.MANAGER);
    Room room = roomService.lock(roomId);
    actor.requireHotelAccess(room.getHotelId());
    if (room.getState() != RoomState.CLEANING) {
      log.warn("Cleaning rejected: room does not require cleaning; roomId={}, state={}",
        roomId, room.getState());
      throw new ResponseStatusException(CONFLICT, "Номер не потребує клінінгу");
    }
    room.setState(RoomState.FREE);
    TransactionLog.info(log, "Cleaning completed: roomId={}, actorId={}",
      roomId, actor.getUserId());
    return room;
  }
}
