package ua.edu.hotels.security;

import lombok.Value;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.enums.Role;

import java.security.Principal;
import java.util.Objects;

import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * Immutable identity for one verified request; contains neither JWT nor password.
 */
@Value
public class HotelToken implements Principal {
  Long userId;
  String email;
  Role role;
  Long hotelId;

  /**
   * Tests exact membership; roles do not inherit privileges from one another.
   */
  public boolean hasRole(Role target) {
    return target != null && role == target;
  }

  /**
   * Enforces exact role permissions without inheriting another role's privileges.
   */
  public void requireRole(Role expected) {
    if (!hasRole(expected)) throw new ResponseStatusException(FORBIDDEN, "Недостатньо прав");
  }

  public void requireHotelAccess(Long requestedHotelId) {
    if (hasRole(Role.MANAGER) && !Objects.equals(hotelId, requestedHotelId))
      throw new ResponseStatusException(FORBIDDEN, "Доступ лише до свого готелю");
  }

  public Long hotelFilter(Long requestedHotelId) {
    if (requestedHotelId != null) requireHotelAccess(requestedHotelId);
    return hasRole(Role.MANAGER) ? hotelId : requestedHotelId;
  }

  public void requireRevenueAccess() {
    if (hasRole(Role.ADMIN))
      throw new ResponseStatusException(
        FORBIDDEN, "Фінансові дані доступні Адміністратору та Керуючому");
  }

  @Override
  public String getName() {
    return email;
  }
}
