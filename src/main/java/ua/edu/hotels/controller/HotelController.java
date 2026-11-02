package ua.edu.hotels.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.*;

/** Exposes hotel maintenance using a verified application principal. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotelController {
  private final HotelService hotelService;

  @GetMapping("/hotels")
  public List<Hotel> hotels(@AuthenticationPrincipal HotelToken token) {
    return hotelService.list(token);
  }

  @PostMapping("/hotels")
  public Hotel hotel(@AuthenticationPrincipal HotelToken token, @Valid @RequestBody HotelInput in) {
    return hotelService.save(token, null, in);
  }

  @PutMapping("/hotels/{id}")
  public Hotel hotel(
      @AuthenticationPrincipal HotelToken token,
      @PathVariable Long id,
      @Valid @RequestBody HotelInput in) {
    return hotelService.save(token, id, in);
  }
}
