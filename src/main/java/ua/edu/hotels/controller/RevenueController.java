package ua.edu.hotels.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.*;

/** Exposes gross revenue summaries for completed stays within the permitted hotel scope. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RevenueController {
  private final RevenueService revenueService;

  @GetMapping("/revenue")
  public RevenueSummary revenue(
      @AuthenticationPrincipal HotelToken token,
      @RequestParam LocalDate start,
      @RequestParam LocalDate end,
      @RequestParam(required = false) Long hotelId) {
    return revenueService.summarize(token, start, end, hotelId);
  }
}
