package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.BookingInput;
import ua.edu.hotels.data.dto.SearchResult;
import ua.edu.hotels.data.entity.Booking;
import ua.edu.hotels.data.entity.Room;
import ua.edu.hotels.data.enums.BookingStatus;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.BookingRepository;
import ua.edu.hotels.security.HotelToken;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * Manages future reservations independently of the current physical room state.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {
  private static final List<BookingStatus> ACTIVE_STATUSES =
    List.of(BookingStatus.RESERVED, BookingStatus.CHECKED_IN);
  private final BookingRepository bookingRepository;
  private final RoomService roomService;
  private final DateRangePolicy dateRangePolicy;

  public boolean overlaps(Long roomId, LocalDate start, LocalDate end, Long excludedId) {
    return bookingRepository.hasOverlap(roomId, start, end, excludedId, ACTIVE_STATUSES);
  }

  public SearchResult search(
    HotelToken actor, LocalDate start, LocalDate end, int beds, Long hotelId) {
    actor.requireRole(Role.ADMINISTRATOR);
    dateRangePolicy.validateBooking(start, end);
    if (beds < 1 || beds > 20) {
      log.warn("Room search rejected: bed count must be between 1 and 20; beds={}", beds);
      throw new ResponseStatusException(BAD_REQUEST, "Кількість ліжок: від 1 до 20");
    }
    List<Room> exact = new ArrayList<>();
    List<Room> larger = new ArrayList<>();
    for (Room room : roomService.list(actor, hotelId)) {
      if (room.getBeds() < beds || overlaps(room.getId(), start, end, null)) continue;
      (room.getBeds() == beds ? exact : larger).add(room);
    }
    return new SearchResult(exact, larger);
  }

  public List<Booking> list(HotelToken actor, Long hotelId) {
    var ids = roomService.list(actor, hotelId).stream().map(Room::getId).toList();
    return ids.isEmpty() ? List.of() : bookingRepository.findByRoomIdInOrderByStartDateDesc(ids);
  }

  @Transactional
  public Booking save(HotelToken actor, Long id, BookingInput input) {
    actor.requireRole(Role.ADMINISTRATOR);
    dateRangePolicy.validateBooking(input.startDate(), input.endDate());
    // Lock before checking overlap so simultaneous requests cannot both reserve the same interval.
    Room room = roomService.lock(input.roomId());
    Booking booking =
      id == null
        ? new Booking()
        : bookingRepository.findById(id).orElseThrow(ServiceErrors::notFound);
    if (id != null) {
      if (!booking.getRoomId().equals(room.getId())) {
        log.warn("Booking update rejected: moving a booking to another room is not allowed; bookingId={}", id);
        throw new ResponseStatusException(
          BAD_REQUEST, "Для зміни номера скасуйте бронювання та створіть нове");
      }
      requireReserved(booking);
    }
    if (overlaps(room.getId(), input.startDate(), input.endDate(), id)) {
      log.warn("Booking rejected: requested dates overlap an active booking; bookingId={}, roomId={}",
        id, room.getId());
      throw new ResponseStatusException(CONFLICT, "Номер уже заброньовано на ці дати");
    }
    booking.setRoomId(room.getId());
    booking.setStartDate(input.startDate());
    booking.setEndDate(input.endDate());
    Booking saved = bookingRepository.save(booking);
    TransactionLog.info(log, "Booking {}: bookingId={}, roomId={}, actorId={}",
      id == null ? "created" : "updated", saved.getId(), room.getId(), actor.getUserId());
    return saved;
  }

  @Transactional
  public Booking cancel(HotelToken actor, Long id) {
    actor.requireRole(Role.ADMINISTRATOR);
    // Scalar lookup avoids loading a stale entity before waiting for the room lock.
    Long roomId = bookingRepository.findRoomIdById(id).orElseThrow(ServiceErrors::notFound);
    roomService.lock(roomId);
    Booking booking = bookingRepository.findById(id).orElseThrow(ServiceErrors::notFound);
    requireReserved(booking);
    booking.setStatus(BookingStatus.CANCELLED);
    TransactionLog.info(log, "Booking cancelled: bookingId={}, roomId={}, actorId={}",
      id, roomId, actor.getUserId());
    return booking;
  }

  private void requireReserved(Booking booking) {
    if (booking.getStatus() != BookingStatus.RESERVED) {
      log.warn("Booking change rejected: booking is no longer reserved; bookingId={}, status={}",
        booking.getId(), booking.getStatus());
      throw new ResponseStatusException(
        CONFLICT, "Змінювати або скасовувати можна лише бронювання до заселення");
    }
  }
}
