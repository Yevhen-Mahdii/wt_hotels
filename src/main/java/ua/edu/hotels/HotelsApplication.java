package ua.edu.hotels;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import ua.edu.hotels.service.RoleService;

/**
 * Starts the service and synchronizes the role catalog after application startup.
 */
@SpringBootApplication
@RequiredArgsConstructor
public class HotelsApplication {
  private final RoleService roleService;

  public static void main(String[] args) {
    SpringApplication.run(HotelsApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void setup() {
    roleService.initialize();
  }
}
