package ua.edu.hotels.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Maps expected failures to client-safe JSON messages without exposing database details.
 */
@RestControllerAdvice
@Slf4j
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<?> status(ResponseStatusException e) {
    if (e.getStatusCode().is5xxServerError()) {
      log.error("Request failed: status={}", e.getStatusCode().value(), e);
    }
    return ResponseEntity.status(e.getStatusCode())
      .body(Map.of("message", e.getReason() == null ? "Запит відхилено" : e.getReason()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  ResponseEntity<?> invalid(Exception e) {
    log.warn("Request rejected: invalid or missing input");
    return ResponseEntity.badRequest()
      .body(
        Map.of(
          "message",
          "Перевірте обов’язкові поля, формат email, довжину пароля (10–72), дати та числа"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> duplicate(Exception e) {
    log.warn("Database write rejected: integrity constraint violated");
    return ResponseEntity.status(409)
      .body(
        Map.of(
          "message",
          "Такий email або номер у готелі вже існує, або дані порушують обмеження"));
  }

  @ExceptionHandler(PessimisticLockingFailureException.class)
  ResponseEntity<?> busy(Exception e) {
    log.warn("Database operation rejected: lock contention; retry the request");
    return ResponseEntity.status(409)
      .body(
        Map.of(
          "message",
          "Номер зараз змінюється іншим користувачем. Оновіть дані та повторіть дію"));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<?> unexpected(Exception e) throws Exception {
    // Preserve Spring's HTTP and security exception handling.
    if (e instanceof ErrorResponse || e instanceof AccessDeniedException) throw e;
    log.error("Unexpected request failure", e);
    return ResponseEntity.internalServerError()
      .body(Map.of("message", "Внутрішня помилка сервера"));
  }
}
