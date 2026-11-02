package ua.edu.hotels.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.*;

/** Exposes combined reservation and occupancy calendars for the authorized hotel scope. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CalendarController {
  private final CalendarService calendarService;

  @GetMapping("/calendar")
  public List<CalendarRow> calendar(
      @AuthenticationPrincipal HotelToken token,
      @RequestParam LocalDate start,
      @RequestParam LocalDate end,
      @RequestParam(required = false) Long hotelId) {
    return calendarService.calendar(token, start, end, hotelId);
  }
}
