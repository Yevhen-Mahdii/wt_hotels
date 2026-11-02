package ua.edu.hotels.service;

import static org.assertj.core.api.Assertions.*;
import static ua.edu.hotels.support.Fixtures.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.enums.Role;

/** Checks role restrictions and prevents managers from accessing another hotel. */
class AccessPolicyTest {
  private final AccessPolicy accessPolicy = new AccessPolicy();

  @ParameterizedTest
  @EnumSource(Role.class)
  void acceptsMatchingRole(Role role) {
    assertThatCode(() -> accessPolicy.requireRole(user(role), role)).doesNotThrowAnyException();
  }

  @Test
  void rejectsWrongRole() {
    assertThatThrownBy(() -> accessPolicy.requireRole(user(Role.MANAGER), Role.ADMINISTRATOR))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void managerIsAlwaysScopedToOwnHotel() {
    assertThat(accessPolicy.hotelFilter(user(Role.MANAGER), null)).isEqualTo(10L);
  }

  @Test
  void managerCannotSelectAnotherHotel() {
    assertThatThrownBy(() -> accessPolicy.hotelFilter(user(Role.MANAGER), 11L))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void administratorCanSelectWholeNetwork() {
    assertThat(accessPolicy.hotelFilter(user(Role.ADMINISTRATOR), null)).isNull();
  }

  @Test
  void systemAdminCannotReadRevenue() {
    assertThatThrownBy(() -> accessPolicy.requireRevenueAccess(user(Role.ADMIN)))
        .isInstanceOf(ResponseStatusException.class);
  }
}
