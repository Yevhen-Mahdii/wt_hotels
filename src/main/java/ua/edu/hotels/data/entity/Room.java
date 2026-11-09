package ua.edu.hotels.data.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.edu.hotels.data.enums.Equipment;
import ua.edu.hotels.data.enums.RoomState;

import java.math.BigDecimal;

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
