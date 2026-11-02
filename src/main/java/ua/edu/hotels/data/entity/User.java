package ua.edu.hotels.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.edu.hotels.data.enums.*;

/** Stores a staff account; password hashes and role relationships are excluded from JSON. */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "AppUser")
@Table(name = "app_user")
public class User extends Identified {
  @Column(nullable = false, unique = true, length = 180)
  private String email;

  @JsonIgnore
  @Column(nullable = false, length = 100)
  private String passwordHash;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "role", nullable = false)
  private UserRole assignedRole;

  // Expose the enum name to JSON without serializing the shared role entity and its users.
  @Transient
  public Role getRole() {
    return assignedRole.getType();
  }

  private Long hotelId;
}
