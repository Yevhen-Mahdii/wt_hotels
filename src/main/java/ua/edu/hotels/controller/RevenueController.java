package ua.edu.hotels.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.hotels.data.dto.RevenueSummary;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.RevenueService;

import java.time.LocalDate;

/**
 * Exposes gross revenue summaries for completed stays within the permitted hotel scope.
 */
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
