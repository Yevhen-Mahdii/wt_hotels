package ua.edu.hotels.data.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.edu.hotels.data.enums.*;

/**
 * Stores current physical state and amenities; future reservations are separate booking records.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Room")
@Table(name = "room")
public class Room extends Identified {
  @Column(nullable = false)
  private Long hotelId;

  @Column(nullable = false, length = 30)
  private String number;

  private int beds;

  @Column(precision = 12, scale = 2, nullable = false)
  private BigDecimal price;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "room_equipment", joinColumns = @JoinColumn(name = "room_id"))
  @Column(name = "equipment_code", nullable = false)
  @Convert(converter = ua.edu.hotels.data.converter.EquipmentConverter.class)
  private java.util.Set<Equipment> equipment = new java.util.HashSet<>();

  @Column(nullable = false)
  private RoomState state = RoomState.FREE;
}
