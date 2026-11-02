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

/** Exposes room maintenance; physical state transitions use the stay endpoints. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {
  private final RoomService roomService;

  @GetMapping("/rooms")
  public List<Room> rooms(
      @AuthenticationPrincipal HotelToken token, @RequestParam(required = false) Long hotelId) {
    return roomService.list(token, hotelId);
  }

  @PostMapping("/rooms")
  public Room room(@AuthenticationPrincipal HotelToken token, @Valid @RequestBody RoomInput in) {
    return roomService.save(token, null, in);
  }

  @PutMapping("/rooms/{id}")
  public Room room(
      @AuthenticationPrincipal HotelToken token,
      @PathVariable Long id,
      @Valid @RequestBody RoomInput in) {
    return roomService.save(token, id, in);
  }
}
