package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.data.dto.RevenueSummary;
import ua.edu.hotels.data.entity.Revenue;
import ua.edu.hotels.repository.RevenueRepository;
import ua.edu.hotels.security.HotelToken;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregates completed-stay revenue by checkout time within noon-to-noon boundaries.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueService {
  private final RevenueRepository revenueRepository;
  private final DateRangePolicy dateRangePolicy;
  private final StayCalculator stayCalculator;

  public RevenueSummary summarize(HotelToken actor, LocalDate start, LocalDate end, Long hotelId) {
    actor.requireRevenueAccess();
    dateRangePolicy.validate(start, end);
    Long filter = actor.hotelFilter(hotelId);
    var items =
      revenueRepository.findForPeriod(
        stayCalculator.boundary(start), stayCalculator.boundary(end), filter);
    BigDecimal total =
      items.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new RevenueSummary(total, items);
  }
}
