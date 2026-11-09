package ua.edu.hotels.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.hotels.data.dto.UserInput;
import ua.edu.hotels.data.entity.User;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.UserService;

import java.util.List;

/**
 * Exposes staff account management; system administrator access is enforced by the service.
 */
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
