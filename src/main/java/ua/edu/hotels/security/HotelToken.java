package ua.edu.hotels.security;

import java.security.Principal;
import lombok.Value;
import ua.edu.hotels.data.enums.Role;

/** Immutable identity for one verified request; contains neither JWT nor password. */
@Value
public class HotelToken implements Principal {
  Long userId;
  String email;
  Role role;
  Long hotelId;

  /** Tests exact membership; roles do not inherit privileges from one another. */
  public boolean hasRole(Role target) {
    return target != null && role == target;
  }

  @Override
  public String getName() {
    return email;
  }
}
