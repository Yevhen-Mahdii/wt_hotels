package ua.edu.hotels.security;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ua.edu.hotels.data.enums.Role;

/** Checks exact role matching and the principal identity contract. */
@ExtendWith(MockitoExtension.class)
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

}
