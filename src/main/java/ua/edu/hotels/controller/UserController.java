package ua.edu.hotels.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.*;

/** Exposes staff account management; system administrator access is enforced by the service. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping("/users")
  public List<User> users(@AuthenticationPrincipal HotelToken token) {
    return userService.list(token);
  }

  @PostMapping("/users")
  public User user(@AuthenticationPrincipal HotelToken token, @Valid @RequestBody UserInput in) {
    return userService.create(token, in);
  }
}
