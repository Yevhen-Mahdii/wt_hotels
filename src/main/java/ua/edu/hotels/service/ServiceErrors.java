package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.server.ResponseStatusException;

/** Provides consistent not-found responses for service lookups. */
final class ServiceErrors {
  private ServiceErrors() {}

  static ResponseStatusException notFound() {
    return new ResponseStatusException(NOT_FOUND, "Запис не знайдено");
  }
}
