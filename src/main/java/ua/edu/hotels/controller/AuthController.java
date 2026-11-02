package ua.edu.hotels.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.*;

/** Exposes initial setup, login and the authenticated user profile. */
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
