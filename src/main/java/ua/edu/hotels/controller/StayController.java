package ua.edu.hotels.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.*;

/** Exposes check-in, checkout and cleaning completion as explicit lifecycle operations. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StayController {
  private final StayService stayService;

  @PostMapping("/rooms/{id}/check-in")
  public Stay checkIn(
      @AuthenticationPrincipal HotelToken token,
      @PathVariable Long id,
      @RequestBody CheckInInput in) {
    return stayService.checkIn(token, id, in.bookingId());
  }

  @PostMapping("/rooms/{id}/check-out")
  public Revenue checkOut(@AuthenticationPrincipal HotelToken token, @PathVariable Long id) {
    return stayService.checkOut(token, id);
  }

  @PostMapping("/rooms/{id}/clean")
  public Room clean(@AuthenticationPrincipal HotelToken token, @PathVariable Long id) {
    return stayService.completeCleaning(token, id);
  }
}
