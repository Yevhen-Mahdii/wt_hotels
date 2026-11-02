package ua.edu.hotels.data.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.edu.hotels.data.enums.*;

/** Represents one hotel within the single managed network. */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Hotel")
@Table(name = "hotel")
public class Hotel extends Identified {
  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 250)
  private String address;
}
