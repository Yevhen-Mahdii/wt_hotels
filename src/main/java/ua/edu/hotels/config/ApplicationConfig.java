package ua.edu.hotels.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Provides an injectable business clock so time-zone rules can be tested deterministically.
 */
@Configuration
public class ApplicationConfig {
  @Bean
  Clock clock(@Value("${app.zone}") String zone) {
    return Clock.system(ZoneId.of(zone));
  }
}
