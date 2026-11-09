package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.AuthResponse;
import ua.edu.hotels.data.dto.Credentials;
import ua.edu.hotels.data.entity.User;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.SetupLockRepository;
import ua.edu.hotels.repository.UserRepository;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Handles login and serializes first-run administrator creation in a database transaction.
 */
@Service
@Slf4j
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
    if (!needsSetup()) {
      log.warn("Initial setup rejected: system is already configured");
      throw new ResponseStatusException(CONFLICT, "Систему вже налаштовано");
    }
    User user = new User();
    user.setEmail(PasswordService.normalizeEmail(input.email()));
    user.setPasswordHash(passwordService.hash(input.password()));
    user.setAssignedRole(roleService.get(Role.ADMIN));
    userRepository.save(user);
    TransactionLog.info(log, "Initial administrator created: userId={}", user.getId());
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
    if (token == null) {
      log.warn("Current user lookup rejected: authentication is missing");
      throw new ResponseStatusException(UNAUTHORIZED);
    }
    return userRepository
      .findById(token.getUserId())
      .orElseThrow(() -> {
        log.warn("Current user lookup rejected: user no longer exists; userId={}", token.getUserId());
        return new ResponseStatusException(UNAUTHORIZED);
      });
  }

  private ResponseStatusException invalidCredentials() {
    log.warn("Login rejected: invalid credentials");
    return new ResponseStatusException(UNAUTHORIZED, "Невірний email або пароль");
  }
}
