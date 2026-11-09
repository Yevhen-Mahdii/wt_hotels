package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ua.edu.hotels.data.entity.User;

import java.time.Duration;
import java.time.Instant;

/**
 * Issues short-lived identity tokens; authorization is resolved from the database per request.
 */
@Service
@RequiredArgsConstructor
public class JwtService {
  private static final Duration TOKEN_LIFETIME = Duration.ofHours(8);
  private final JwtEncoder jwtEncoder;

  public String issue(User user) {
    Instant now = Instant.now();
    var claims =
      JwtClaimsSet.builder()
        .issuer("wt-hotels")
        .subject(user.getId().toString())
        .issuedAt(now)
        .expiresAt(now.plus(TOKEN_LIFETIME))
        .build();
    return jwtEncoder
      .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
      .getTokenValue();
  }
}
