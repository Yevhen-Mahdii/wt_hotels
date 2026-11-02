package ua.edu.hotels.support;

import java.math.BigDecimal;
import java.time.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.security.HotelToken;

/** Provides a fixed Kyiv clock and role-scoped principals for deterministic unit tests. */
public final class Fixtures {
  public static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-05T10:00:00Z"), ZoneId.of("Europe/Kyiv"));

  private Fixtures() {}

  public static HotelToken user(Role role) {
    return new HotelToken(1L, "test@example.test", role, role == Role.MANAGER ? 10L : null);
  }

  public static Room room(RoomState state) {
    var room = new Room();
    room.setId(20L);
    room.setHotelId(10L);
    room.setBeds(2);
    room.setPrice(new BigDecimal("1200.00"));
    room.setState(state);
    return room;
  }

  public static Booking booking() {
    var booking = new Booking();
    booking.setId(30L);
    booking.setRoomId(20L);
    booking.setStartDate(LocalDate.of(2026, 11, 5));
    booking.setEndDate(LocalDate.of(2026, 11, 8));
    return booking;
  }

  public static Stay stay() {
    var stay = new Stay();
    stay.setId(40L);
    stay.setRoomId(20L);
    stay.setCheckedInAt(Instant.parse("2026-11-02T10:00:00Z"));
    stay.setPrice(new BigDecimal("1000.00"));
    return stay;
  }
}
