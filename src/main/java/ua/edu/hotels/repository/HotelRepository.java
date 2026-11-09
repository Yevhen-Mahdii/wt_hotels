package ua.edu.hotels.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.hotels.data.entity.Hotel;

import java.util.List;

/**
 * Provides hotel persistence and deterministic display ordering.
 */
public interface HotelRepository extends JpaRepository<Hotel, Long> {
  List<Hotel> findAllByOrderByNameAsc();
}
