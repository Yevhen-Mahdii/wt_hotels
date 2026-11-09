package ua.edu.hotels.security;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.UserRepository;

/**
 * Checks database-derived permissions and rejection of missing users or malformed token
 * subjects.
 */
@ExtendWith(MockitoExtension.class)
class HotelTokenConverterTest {
  @Mock
  UserRepository userRepository;
  @InjectMocks
  HotelTokenConverter hotelTokenConverter;

  private Jwt jwt(String subject) {
    return Jwt.withTokenValue("verified-jwt")
      .header("alg", "HS256")
      .subject(subject)
      .claim("role", "ADMIN")
      .build();
  }

  @Test
  void usesDatabaseIdentityNotRoleClaims() {
    User user = new User();
    user.setId(5L);
    user.setEmail("manager@example.test");
    user.setAssignedRole(UserRole.from(Role.MANAGER));
    user.setHotelId(10L);
    when(userRepository.findById(5L)).thenReturn(Optional.of(user));
    var authentication = hotelTokenConverter.convert(jwt("5"));
    HotelToken token = (HotelToken) authentication.getPrincipal();
    assertThat(token.hasRole(Role.MANAGER)).isTrue();
    assertThat(token.hasRole(Role.ADMIN)).isFalse();
    assertThat(token.getHotelId()).isEqualTo(10L);
    assertThat(token.getUserId()).isEqualTo(5L);
    assertThat(authentication.isAuthenticated()).isTrue();
    assertThat(authentication.getCredentials()).isEqualTo("");
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "-1", "0", "99999999999999999999999"})
  void rejectsInvalidSubject(String subject) {
    assertThatThrownBy(() -> hotelTokenConverter.convert(jwt(subject)))
      .isInstanceOf(InvalidBearerTokenException.class);
    verifyNoInteractions(userRepository);
  }

  @Test
  void rejectsMissingUser() {
    assertThatThrownBy(() -> hotelTokenConverter.convert(jwt("5")))
      .isInstanceOf(InvalidBearerTokenException.class);
  }
}
