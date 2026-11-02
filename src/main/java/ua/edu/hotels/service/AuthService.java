package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.*;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.repository.*;

/** Handles login and serializes first-run administrator creation in a database transaction. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
  private final UserRepository userRepository;
  private final SetupLockRepository setupLockRepository;
  private final PasswordService passwordService;
  private final JwtService jwtService;
  private final RoleService roleService;

  public boolean needsSetup() {
    return userRepository.count() == 0;
  }

  @Transactional
  public AuthResponse setup(Credentials input) {
    setupLockRepository.lock();
    if (!needsSetup()) throw new ResponseStatusException(CONFLICT, "Систему вже налаштовано");
    User user = new User();
    user.setEmail(PasswordService.normalizeEmail(input.email()));
    user.setPasswordHash(passwordService.hash(input.password()));
    user.setAssignedRole(roleService.get(Role.ADMIN));
    userRepository.save(user);
    return new AuthResponse(jwtService.issue(user), user);
  }

  public AuthResponse login(Credentials input) {
    User user =
        userRepository
            .findByEmail(PasswordService.normalizeEmail(input.email()))
            .orElseThrow(this::invalidCredentials);
    if (!passwordService.matches(input.password(), user.getPasswordHash()))
      throw invalidCredentials();
    return new AuthResponse(jwtService.issue(user), user);
  }

  public User current(ua.edu.hotels.security.HotelToken token) {
    if (token == null) throw new ResponseStatusException(UNAUTHORIZED);
    return userRepository
        .findById(token.getUserId())
        .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED));
  }

  private ResponseStatusException invalidCredentials() {
    return new ResponseStatusException(UNAUTHORIZED, "Невірний email або пароль");
  }
}
