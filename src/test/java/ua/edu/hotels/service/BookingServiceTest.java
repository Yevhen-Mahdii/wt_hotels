package ua.edu.hotels.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ua.edu.hotels.support.Fixtures.*;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.BookingInput;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.repository.BookingRepository;

/** Checks booking permissions, overlap rejection and allowed reservation transitions. */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
  @Mock BookingRepository bookingRepository;
  @Mock RoomService roomService;
  BookingService bookingService;

  @BeforeEach
  void setup() {
    bookingService =
        new BookingService(
            bookingRepository, roomService, new DateRangePolicy(CLOCK));
  }

  BookingInput input() {
    return new BookingInput(20L, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 8));
  }

  @Test
  void onlyAdministratorCanBook() {
    assertThatThrownBy(() -> bookingService.save(user(Role.MANAGER), null, input()))
        .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(roomService, bookingRepository);
  }

  @Test
  void checksOverlapAfterLockingRoom() {
    when(roomService.lock(20L)).thenReturn(room(RoomState.FREE));
    when(bookingRepository.hasOverlap(eq(20L), any(), any(), isNull(), anyList())).thenReturn(true);
    assertThatThrownBy(() -> bookingService.save(user(Role.ADMINISTRATOR), null, input()))
        .isInstanceOf(ResponseStatusException.class);
    var order = inOrder(roomService, bookingRepository);
    order.verify(roomService).lock(20L);
    order.verify(bookingRepository).hasOverlap(eq(20L), any(), any(), isNull(), anyList());
    verify(bookingRepository, never()).save(any());
  }

  @Test
  void createsReservationWithoutPriceSnapshot() {
    when(roomService.lock(20L)).thenReturn(room(RoomState.FREE));
    when(bookingRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    Booking booking = bookingService.save(user(Role.ADMINISTRATOR), null, input());
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.RESERVED);
    assertThat(booking.getStartDate()).isEqualTo(input().startDate());
  }

  @Test
  void searchSeparatesExactAndLargerRooms() {
    Room exact = room(RoomState.FREE), larger = room(RoomState.FREE), small = room(RoomState.FREE);
    larger.setId(21L);
    larger.setBeds(3);
    small.setId(22L);
    small.setBeds(1);
    when(roomService.list(any(), isNull())).thenReturn(List.of(small, larger, exact));
    var result =
        bookingService.search(
            user(Role.ADMINISTRATOR), input().startDate(), input().endDate(), 2, null);
    assertThat(result.exact()).containsExactly(exact);
    assertThat(result.larger()).containsExactly(larger);
  }

  @Test
  void cancellationLocksBeforeLoadingEntity() {
    Booking booking = booking();
    when(bookingRepository.findRoomIdById(30L)).thenReturn(Optional.of(20L));
    when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));
    bookingService.cancel(user(Role.ADMINISTRATOR), 30L);
    var order = inOrder(roomService, bookingRepository);
    order.verify(bookingRepository).findRoomIdById(30L);
    order.verify(roomService).lock(20L);
    order.verify(bookingRepository).findById(30L);
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
  }

  @Test
  void cannotCancelCheckedInBooking() {
    Booking booking = booking();
    booking.setStatus(BookingStatus.CHECKED_IN);
    when(bookingRepository.findRoomIdById(30L)).thenReturn(Optional.of(20L));
    when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));
    assertThatThrownBy(() -> bookingService.cancel(user(Role.ADMINISTRATOR), 30L))
        .isInstanceOf(ResponseStatusException.class);
  }
}
