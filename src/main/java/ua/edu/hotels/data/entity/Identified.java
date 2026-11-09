package ua.edu.hotels.data.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Shares database-generated primary keys across business entities.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class Identified {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
