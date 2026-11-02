package ua.edu.hotels.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.repository.UserRepository;

/** Builds a request principal from a verified JWT and the current database user record. */
@Component
@RequiredArgsConstructor
public class HotelTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Long userId = parseSubject(jwt.getSubject());
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new InvalidBearerTokenException("User no longer exists"));
    // Read permissions from the database for each request, not from client claims.
    var principal =
        new HotelToken(user.getId(), user.getEmail(), user.getRole(), user.getHotelId());
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRole().name()));
    return UsernamePasswordAuthenticationToken.authenticated(principal, "", authorities);
  }

  private Long parseSubject(String subject) {
    try {
      long value = Long.parseLong(subject);
      if (value > 0) return value;
    } catch (NumberFormatException ignored) {
      /* Convert malformed identities to HTTP 401. */
    }
    throw new InvalidBearerTokenException("Invalid user identity");
  }
}
