package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Hashes passwords with the configured encoder and normalizes login email addresses. */
@Service
@RequiredArgsConstructor
public class PasswordService {
  private final PasswordEncoder passwordEncoder;

  public String hash(String password) {
    // BCrypt limits bytes, not Java characters; reject oversized UTF-8 input before hashing.
    if (password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ResponseStatusException(
          BAD_REQUEST, "Пароль має містити не більше 72 байтів UTF-8");
    return passwordEncoder.encode(password);
  }

  public boolean matches(String password, String hash) {
    return passwordEncoder.matches(password, hash);
  }

  public static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
