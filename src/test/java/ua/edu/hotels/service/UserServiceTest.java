package ua.edu.hotels.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ua.edu.hotels.support.Fixtures.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.UserInput;
import ua.edu.hotels.data.entity.UserRole;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.UserRepository;

/**
 * Checks role assignment and account-creation rules, not repository delegation.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  UserRepository users;
  @Mock
  HotelService hotels;
  @Mock
  PasswordService passwords;
  @Mock
  RoleService roles;
  @InjectMocks
  UserService service;

  @ParameterizedTest
  @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "MANAGER"})
  void onlyAdminCanCreateStaff(Role actorRole) {
    var input = new UserInput("staff@example.test", "long-password", Role.MANAGER, 10L);
    assertThatThrownBy(() -> service.create(user(actorRole), input))
      .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(users, hotels, passwords, roles);
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, names = {"ADMIN", "ADMINISTRATOR"})
  void nonManagerCannotHaveHotelAssignment(Role role) {
    var input = new UserInput("staff@example.test", "long-password", role, 10L);
    assertThatThrownBy(() -> service.create(user(Role.ADMIN), input))
      .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(users, hotels, passwords, roles);
  }

  @Test
  void managerReceivesRequestedHotelAndNormalizedCredentials() {
    when(passwords.hash("long-password")).thenReturn("hash");
    when(roles.get(Role.MANAGER)).thenReturn(UserRole.from(Role.MANAGER));
    when(users.save(any())).thenAnswer(call -> call.getArgument(0));
    var result = service.create(user(Role.ADMIN),
      new UserInput(" STAFF@Example.test ", "long-password", Role.MANAGER, 10L));
    assertThat(result.getHotelId()).isEqualTo(10L);
    assertThat(result.getRole()).isEqualTo(Role.MANAGER);
    assertThat(result.getEmail()).isEqualTo("staff@example.test");
    assertThat(result.getPasswordHash()).isEqualTo("hash");
  }
}
