package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.repository.*;
import ua.edu.hotels.security.HotelToken;

/** Aggregates completed-stay revenue by checkout time within noon-to-noon boundaries. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueService {
  private final RevenueRepository revenueRepository;
  private final AccessPolicy accessPolicy;
  private final DateRangePolicy dateRangePolicy;
  private final StayCalculator stayCalculator;

  public RevenueSummary summarize(HotelToken actor, LocalDate start, LocalDate end, Long hotelId) {
    accessPolicy.requireRevenueAccess(actor);
    dateRangePolicy.validate(start, end);
    Long filter = accessPolicy.hotelFilter(actor, hotelId);
    var items =
        revenueRepository.findForPeriod(
            stayCalculator.boundary(start), stayCalculator.boundary(end), filter);
    BigDecimal total =
        items.stream().map(Revenue::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new RevenueSummary(total, items);
  }
}
