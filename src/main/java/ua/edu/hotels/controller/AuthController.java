package ua.edu.hotels.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.hotels.data.dto.AuthResponse;
import ua.edu.hotels.data.dto.Credentials;
import ua.edu.hotels.data.dto.SetupStatus;
import ua.edu.hotels.data.entity.User;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.AuthService;

/**
 * Exposes initial setup, login and the authenticated user profile.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @GetMapping("/auth/status")
  public SetupStatus status() {
    return new SetupStatus(authService.needsSetup());
  }

  @PostMapping("/auth/setup")
  public AuthResponse setup(@Valid @RequestBody Credentials in) {
    return authService.setup(in);
  }

  @PostMapping("/auth/login")
  public AuthResponse login(@Valid @RequestBody Credentials in) {
    return authService.login(in);
  }

  @GetMapping("/me")
  public User me(@AuthenticationPrincipal HotelToken token) {
    return authService.current(token);
  }
}
