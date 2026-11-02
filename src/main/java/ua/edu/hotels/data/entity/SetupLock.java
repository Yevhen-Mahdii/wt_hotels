package ua.edu.hotels.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Maps the permanent singleton row created by Flyway for first-run setup serialization. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "setup_lock")
public class SetupLock {
  @Id private Integer id;
}
