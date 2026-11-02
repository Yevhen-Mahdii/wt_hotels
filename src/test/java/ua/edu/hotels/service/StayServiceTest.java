package ua.edu.hotels.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ua.edu.hotels.support.Fixtures.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.repository.*;

/** Checks occupancy transitions, check-in pricing and checkout revenue recording. */
@ExtendWith(MockitoExtension.class)
class StayServiceTest {
  @Mock RoomService roomService;
  @Mock StayRepository stayRepository;
  @Mock BookingRepository bookingRepository;
  @Mock BookingService bookingService;
  @Mock RevenueRepository revenueRepository;
  StayService stayService;

  @BeforeEach
  void setup() {
    stayService =
        new StayService(
            roomService,
            stayRepository,
            bookingRepository,
            bookingService,
            revenueRepository,
            new AccessPolicy(),
            new StayCalculator(CLOCK),
            CLOCK);
  }

  @Test
  void snapshotsPriceAtCheckIn() {
    Room room = room(RoomState.FREE);
    when(roomService.lock(20L)).thenReturn(room);
    when(stayRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    Stay stay = stayService.checkIn(user(Role.MANAGER), 20L, null);
    room.setPrice(new BigDecimal("2500.00"));
    assertThat(stay.getPrice()).isEqualByComparingTo("1200.00");
    assertThat(stay.getCheckedInAt()).isEqualTo(CLOCK.instant());
    assertThat(room.getState()).isEqualTo(RoomState.OCCUPIED);
    verifyNoInteractions(revenueRepository);
  }

  @Test
  void dirtyRoomCannotBeOccupied() {
    when(roomService.lock(20L)).thenReturn(room(RoomState.CLEANING));
    assertThatThrownBy(() -> stayService.checkIn(user(Role.MANAGER), 20L, null))
        .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(stayRepository, revenueRepository);
  }

  @Test
  void walkInCannotIgnoreExistingBooking() {
    when(roomService.lock(20L)).thenReturn(room(RoomState.FREE));
    when(bookingService.overlaps(eq(20L), any(), any(), isNull())).thenReturn(true);
    assertThatThrownBy(() -> stayService.checkIn(user(Role.MANAGER), 20L, null))
        .isInstanceOf(ResponseStatusException.class);
    verify(stayRepository, never()).save(any());
  }

  @Test
  void matchingBookingIsCheckedIn() {
    Booking booking = booking();
    when(roomService.lock(20L)).thenReturn(room(RoomState.FREE));
    when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));
    stayService.checkIn(user(Role.MANAGER), 20L, 30L);
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CHECKED_IN);
  }

  @Test
  void checkoutUsesActualNightsAndStoredPriceExactlyOnce() {
    Room room = room(RoomState.OCCUPIED);
    Stay stay = stay();
    stay.setBookingId(30L);
    Booking booking = booking();
    booking.setStatus(BookingStatus.CHECKED_IN);
    when(roomService.lock(20L)).thenReturn(room);
    when(stayRepository.findByRoomIdAndCheckedOutAtIsNull(20L)).thenReturn(Optional.of(stay));
    when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));
    when(revenueRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    Revenue revenue = stayService.checkOut(user(Role.MANAGER), 20L);
    assertThat(revenue.getNights()).isEqualTo(3);
    assertThat(revenue.getAmount()).isEqualByComparingTo("3000.00");
    assertThat(room.getState()).isEqualTo(RoomState.CLEANING);
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    assertThatThrownBy(() -> stayService.checkOut(user(Role.MANAGER), 20L))
        .isInstanceOf(ResponseStatusException.class);
    verify(revenueRepository, times(1)).save(any());
  }

  @Test
  void cleaningCompletesWithoutCreatingRevenue() {
    Room room = room(RoomState.CLEANING);
    when(roomService.lock(20L)).thenReturn(room);
    stayService.completeCleaning(user(Role.MANAGER), 20L);
    assertThat(room.getState()).isEqualTo(RoomState.FREE);
    verifyNoInteractions(revenueRepository);
  }

  @Test
  void cannotCleanOccupiedRoom() {
    when(roomService.lock(20L)).thenReturn(room(RoomState.OCCUPIED));
    assertThatThrownBy(() -> stayService.completeCleaning(user(Role.MANAGER), 20L))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void cannotOperateAnotherHotel() {
    Room room = room(RoomState.FREE);
    room.setHotelId(11L);
    when(roomService.lock(20L)).thenReturn(room);
    assertThatThrownBy(() -> stayService.checkIn(user(Role.MANAGER), 20L, null))
        .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(stayRepository, revenueRepository);
  }
}
