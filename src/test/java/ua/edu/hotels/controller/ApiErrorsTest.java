package ua.edu.hotels.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ApiErrorsTest {
  private final ApiErrors errors = new ApiErrors();

  @Test
  void hidesUnexpectedFailureDetails() throws Exception {
    var response = errors.unexpected(new IllegalStateException("Internal diagnostic detail"));
    assertThat(response.getStatusCode().value()).isEqualTo(500);
    assertThat(response.getBody()).isEqualTo(Map.of("message", "Внутрішня помилка сервера"));
  }

  @Test
  void preservesSecurityHandling() {
    var failure = new AccessDeniedException("Access denied");
    assertThatThrownBy(() -> errors.unexpected(failure)).isSameAs(failure);
  }

  @Test
  void preservesFrameworkHttpStatus() {
    var failure = new NoResourceFoundException(HttpMethod.GET, "missing");
    assertThatThrownBy(() -> errors.unexpected(failure)).isSameAs(failure);
  }
}
