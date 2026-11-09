package ua.edu.hotels.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ua.edu.hotels.support.Fixtures.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.edu.hotels.data.entity.Revenue;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.RevenueRepository;

/**
 * Checks reporting scope and aggregation of completed-stay income.
 */
@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {
  @Mock
  RevenueRepository revenueRepository;
  RevenueService revenueService;
  StayCalculator stayCalculator = new StayCalculator(CLOCK);
  LocalDate start = LocalDate.of(2026, 11, 1), end = LocalDate.of(2026, 12, 1);

  @BeforeEach
  void setup() {
    revenueService =
      new RevenueService(
        revenueRepository, new DateRangePolicy(CLOCK), stayCalculator);
  }

  @Test
  void sumsDecimalAmountsAndScopesManager() {
    var first = new Revenue();
    first.setAmount(new BigDecimal("1000.10"));
    var second = new Revenue();
    second.setAmount(new BigDecimal("2000.20"));
    when(revenueRepository.findForPeriod(
      stayCalculator.boundary(start), stayCalculator.boundary(end), 10L))
      .thenReturn(List.of(first, second));
    assertThat(revenueService.summarize(user(Role.MANAGER), start, end, null).total())
      .isEqualByComparingTo("3000.30");
  }

  @Test
  void networkReportUsesNoHotelFilter() {
    when(revenueRepository.findForPeriod(
      stayCalculator.boundary(start), stayCalculator.boundary(end), null))
      .thenReturn(List.of());
    assertThat(revenueService.summarize(user(Role.ADMINISTRATOR), start, end, null).total())
      .isEqualByComparingTo("0");
  }
}
