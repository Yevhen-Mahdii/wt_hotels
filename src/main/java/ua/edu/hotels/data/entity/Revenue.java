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
 * Stores the checkout-time revenue snapshot; each stay can produce only one revenue record.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Revenue")
@Table(name = "revenue")
public class Revenue extends Identified {
  private Long stayId;
  private Long hotelId;
  private Instant recordedAt;
  private long nights;

  @Column(precision = 14, scale = 2)
  private BigDecimal amount;
}
