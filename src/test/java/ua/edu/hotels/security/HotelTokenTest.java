package ua.edu.hotels.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ua.edu.hotels.data.enums.Role;

/** Checks exact role matching and the principal identity contract. */
class HotelTokenTest {
  @ParameterizedTest
  @EnumSource(Role.class)
  void onlyMatchesAssignedRole(Role role) {
    HotelToken token = new HotelToken(1L, "user@example.test", role, 10L);
    for (Role target : Role.values()) assertThat(token.hasRole(target)).isEqualTo(role == target);
  }

  @Test
  void nullDoesNotGrantAccess() {
    assertThat(new HotelToken(1L, "user@example.test", Role.ADMIN, null).hasRole(null)).isFalse();
  }

  @Test
  void principalNameIsEmail() {
    assertThat(new HotelToken(1L, "user@example.test", Role.ADMIN, null).getName())
        .isEqualTo("user@example.test");
  }
}
