package ua.edu.hotels.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

/** Checks password hashing, byte limits and email normalization. */
@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {
  @Mock PasswordEncoder encoder;
  @InjectMocks PasswordService passwords;

  @Test
  void checksUtf8BytesBeforeBcrypt() {
    assertThatThrownBy(() -> passwords.hash("ї".repeat(37)))
        .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(encoder);
  }

  @Test
  void acceptsExactlySeventyTwoUtf8Bytes() {
    String password = "ї".repeat(36);
    when(encoder.encode(password)).thenReturn("encoded");
    assertThat(passwords.hash(password)).isEqualTo("encoded");
  }

  @Test
  void normalizesEmail() {
    assertThat(PasswordService.normalizeEmail(" ADMIN@Example.test "))
        .isEqualTo("admin@example.test");
  }
}
