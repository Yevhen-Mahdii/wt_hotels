package ua.edu.hotels.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.edu.hotels.data.enums.BookingStatus;

import java.time.LocalDate;

/**
 * Stores a planned room reservation with an exclusive departure date and no guest information.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Booking")
@Table(name = "booking")
public class Booking extends Identified {
  private Long roomId;
  private LocalDate startDate;
  private LocalDate endDate;

  @Column(nullable = false)
  private BookingStatus status = BookingStatus.RESERVED;
}
