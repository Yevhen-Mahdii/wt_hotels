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
import org.springframework.web.bind.annotation.RestController;
import ua.edu.hotels.data.dto.HotelInput;
import ua.edu.hotels.data.entity.Hotel;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.HotelService;

import java.util.List;

/**
 * Exposes hotel maintenance using a verified application principal.
 */
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
