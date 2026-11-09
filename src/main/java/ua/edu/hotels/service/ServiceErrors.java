package ua.edu.hotels.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Provides consistent not-found responses for service lookups.
 */
@Slf4j
final class ServiceErrors {
  private ServiceErrors() {
  }

  static ResponseStatusException notFound() {
    log.warn("Operation rejected: requested record was not found");
    return new ResponseStatusException(NOT_FOUND, "Запис не знайдено");
  }
}
