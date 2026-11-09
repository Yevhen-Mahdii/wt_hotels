package ua.edu.hotels.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.hotels.data.dto.CheckInInput;
import ua.edu.hotels.data.entity.Revenue;
import ua.edu.hotels.data.entity.Room;
import ua.edu.hotels.data.entity.Stay;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.StayService;

/**
 * Exposes check-in, checkout and cleaning completion as explicit lifecycle operations.
 */
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
