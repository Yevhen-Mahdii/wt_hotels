package ua.edu.hotels.repository;

import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import ua.edu.hotels.data.entity.Hotel;

/** Provides hotel persistence and deterministic display ordering. */
public interface HotelRepository extends JpaRepository<Hotel, Long> {
  List<Hotel> findAllByOrderByNameAsc();
}
