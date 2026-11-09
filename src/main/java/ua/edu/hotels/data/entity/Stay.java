package ua.edu.hotels.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Records actual occupancy timestamps and the nightly price captured at check-in.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Stay")
@Table(name = "stay")
public class Stay extends Identified {
  private Long roomId;
  private Long bookingId;
  private Instant checkedInAt;
  private Instant checkedOutAt;

  @Column(precision = 12, scale = 2)
  private BigDecimal price;
}
