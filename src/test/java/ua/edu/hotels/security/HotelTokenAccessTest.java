package ua.edu.hotels.security;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static ua.edu.hotels.support.Fixtures.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.enums.Role;

/** Checks role restrictions and prevents managers from accessing another hotel. */
@ExtendWith(MockitoExtension.class)
class HotelTokenAccessTest {

  @ParameterizedTest
  @EnumSource(Role.class)
  void acceptsMatchingRole(Role role) {
    assertThatCode(() -> user(role).requireRole(role)).doesNotThrowAnyException();
  }

  @Test
  void rejectsWrongRole() {
    assertThatThrownBy(() -> user(Role.MANAGER).requireRole(Role.ADMINISTRATOR))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void managerIsAlwaysScopedToOwnHotel() {
    assertThat(user(Role.MANAGER).hotelFilter(null)).isEqualTo(10L);
  }

  @Test
  void managerCannotSelectAnotherHotel() {
    assertThatThrownBy(() -> user(Role.MANAGER).hotelFilter(11L))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void administratorCanSelectWholeNetwork() {
    assertThat(user(Role.ADMINISTRATOR).hotelFilter(null)).isNull();
  }

  @Test
  void systemAdminCannotReadRevenue() {
    assertThatThrownBy(() -> user(Role.ADMIN).requireRevenueAccess())
        .isInstanceOf(ResponseStatusException.class);
  }
}
