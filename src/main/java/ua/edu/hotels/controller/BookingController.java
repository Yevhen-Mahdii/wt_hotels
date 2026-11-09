package ua.edu.hotels.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.hotels.data.dto.BookingInput;
import ua.edu.hotels.data.dto.SearchResult;
import ua.edu.hotels.data.entity.Booking;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.BookingService;

import java.time.LocalDate;
import java.util.List;

/**
 * Accepts reservation requests; role and overlap checks remain in the service layer.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
  private final BookingService bookingService;

  @GetMapping("/rooms/search")
  public SearchResult search(
    @AuthenticationPrincipal HotelToken token,
    @RequestParam LocalDate start,
    @RequestParam LocalDate end,
    @RequestParam int beds,
    @RequestParam(required = false) Long hotelId) {
    return bookingService.search(token, start, end, beds, hotelId);
  }

  @GetMapping("/bookings")
  public List<Booking> bookings(
    @AuthenticationPrincipal HotelToken token, @RequestParam(required = false) Long hotelId) {
    return bookingService.list(token, hotelId);
  }

  @PostMapping("/bookings")
  public Booking book(
    @AuthenticationPrincipal HotelToken token, @Valid @RequestBody BookingInput in) {
    return bookingService.save(token, null, in);
  }

  @PutMapping("/bookings/{id}")
  public Booking book(
    @AuthenticationPrincipal HotelToken token,
    @PathVariable Long id,
    @Valid @RequestBody BookingInput in) {
    return bookingService.save(token, id, in);
  }

  @PostMapping("/bookings/{id}/cancel")
  public Booking cancel(@AuthenticationPrincipal HotelToken token, @PathVariable Long id) {
    return bookingService.cancel(token, id);
  }
}
