package ua.edu.hotels.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.Credentials;
import ua.edu.hotels.data.entity.User;
import ua.edu.hotels.data.entity.UserRole;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.SetupLockRepository;
import ua.edu.hotels.repository.UserRepository;

/**
 * Exercises first-admin and authentication decisions without Spring or database access.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock
  UserRepository users;
  @Mock
  SetupLockRepository setupLock;
  @Mock
  PasswordService passwords;
  @Mock
  JwtService jwt;
  @Mock
  RoleService roles;
  @InjectMocks
  AuthService auth;

  @Test
  void rejectsRepeatedSetupBeforeHashingOrSaving() {
    when(users.count()).thenReturn(1L);
    assertThatThrownBy(() -> auth.setup(new Credentials("admin@example.test", "long-password")))
      .isInstanceOfSatisfying(ResponseStatusException.class,
        error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
    var order = inOrder(setupLock, users);
    order.verify(setupLock).lock();
    order.verify(users).count();
    verify(users, never()).save(any());
    verifyNoInteractions(passwords, jwt, roles);
  }

  @Test
  void firstAccountIsAdminWithNormalizedEmailAndHashedPassword() {
    when(passwords.hash("long-password")).thenReturn("hash");
    when(roles.get(Role.ADMIN)).thenReturn(UserRole.from(Role.ADMIN));
    when(jwt.issue(any())).thenReturn("token");
    var response = auth.setup(new Credentials(" ADMIN@Example.test ", "long-password"));
    assertThat(response.user().getEmail()).isEqualTo("admin@example.test");
    assertThat(response.user().getPasswordHash()).isEqualTo("hash");
    assertThat(response.user().getRole()).isEqualTo(Role.ADMIN);
    assertThat(response.user().getHotelId()).isNull();
    assertThat(response.token()).isEqualTo("token");
  }

  @Test
  void rejectsWrongPasswordWithoutIssuingToken() {
    var user = new User();
    user.setPasswordHash("hash");
    when(users.findByEmail("user@example.test")).thenReturn(Optional.of(user));
    assertThatThrownBy(() -> auth.login(new Credentials(" USER@Example.test ", "wrong-password")))
      .isInstanceOfSatisfying(ResponseStatusException.class,
        error -> assertThat(error.getStatusCode().value()).isEqualTo(401));
    verify(passwords).matches("wrong-password", "hash");
    verifyNoInteractions(jwt);
  }

  @Test
  void missingAccountIsRejectedWithoutPasswordCheckOrToken() {
    assertThatThrownBy(() -> auth.login(new Credentials("missing@example.test", "long-password")))
      .isInstanceOfSatisfying(ResponseStatusException.class,
        error -> assertThat(error.getStatusCode().value()).isEqualTo(401));
    verifyNoInteractions(passwords, jwt);
  }
}
