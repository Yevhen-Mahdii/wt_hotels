package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.security.HotelToken;

/** Enforces role permissions and restricts managers to their assigned hotel. */
@Component
public class AccessPolicy {
  public void requireRole(HotelToken token, Role expected) {
    if (!token.hasRole(expected)) throw new ResponseStatusException(FORBIDDEN, "Недостатньо прав");
  }

  public void requireHotelAccess(HotelToken token, Long hotelId) {
    if (token.hasRole(Role.MANAGER) && !Objects.equals(token.getHotelId(), hotelId))
      throw new ResponseStatusException(FORBIDDEN, "Доступ лише до свого готелю");
  }

  public Long hotelFilter(HotelToken token, Long requestedHotelId) {
    if (requestedHotelId != null) requireHotelAccess(token, requestedHotelId);
    return token.hasRole(Role.MANAGER) ? token.getHotelId() : requestedHotelId;
  }

  public void requireRevenueAccess(HotelToken token) {
    if (token.hasRole(Role.ADMIN))
      throw new ResponseStatusException(
          FORBIDDEN, "Фінансові дані доступні Адміністратору та Керуючому");
  }
}
