package ua.edu.hotels.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

/** Checks password hashing, byte limits and email normalization. */
class PasswordServiceTest {
  @Test
  void checksUtf8BytesBeforeBcrypt() {
    var encoder = mock(PasswordEncoder.class);
    var passwords = new PasswordService(encoder);
    assertThatThrownBy(() -> passwords.hash("ї".repeat(37)))
        .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(encoder);
  }

  @Test
  void delegatesHashingToEncoder() {
    var encoder = mock(PasswordEncoder.class);
    when(encoder.encode("long-password")).thenReturn("encoded");
    assertThat(new PasswordService(encoder).hash("long-password")).isEqualTo("encoded");
  }

  @Test
  void normalizesEmail() {
    assertThat(PasswordService.normalizeEmail(" ADMIN@Example.test "))
        .isEqualTo("admin@example.test");
  }
}
