package ua.edu.hotels.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures stateless bearer authentication, signing-key persistence and browser security
 * headers.
 */
@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecretKeySpec jwtKey(@Value("${app.jwt-key-file}") String file) throws Exception {
    Path path = Path.of(file);
    Files.createDirectories(path.toAbsolutePath().getParent());
    if (!Files.exists(path)) {
      byte[] key = new byte[32];
      new SecureRandom().nextBytes(key);
      Files.writeString(
          path, Base64.getEncoder().encodeToString(key), StandardOpenOption.CREATE_NEW);
    }
    byte[] bytes = Base64.getDecoder().decode(Files.readString(path).trim());
    if (bytes.length < 32)
      throw new IllegalStateException("JWT key must contain at least 32 bytes");
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKeySpec key) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKeySpec key) {
    var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("wt-hotels"));
    return decoder;
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http, ua.edu.hotels.security.HotelTokenConverter hotelTokenConverter)
      throws Exception {
    // Only explicit Bearer tokens are accepted; authentication does not use cookies.
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/",
                        "/index.html",
                        // Navigation cannot attach a Bearer token; only the HTML shell is public.
                        "/pages/*.html",
                        "/fragments/*.html",
                        "/js/**",
                        "/styles.css",
                        "/favicon.ico",
                        "/api/auth/**",
                        "/api/catalog")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(hotelTokenConverter)))
        .headers(
            h ->
                h.contentSecurityPolicy(
                    c ->
                        c.policyDirectives(
                            "default-src 'self'; script-src 'self'; style-src 'self'; img-src"
                                + " 'self' data:; frame-ancestors 'none'; base-uri 'self';"
                                + " form-action 'self'")))
        .build();
  }
}
