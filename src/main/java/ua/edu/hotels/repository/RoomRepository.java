package ua.edu.hotels.repository;

import jakarta.persistence.LockModeType;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ua.edu.hotels.data.entity.Room;

/** Provides the shared room lock used to serialize reservations and occupancy transitions. */
public interface RoomRepository extends JpaRepository<Room, Long> {
  List<Room> findAllByOrderByHotelIdAscNumberAsc();

  List<Room> findByHotelIdOrderByNumberAsc(Long hotelId);

  /** Must run inside the transaction that performs the dependent state change. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from Room r where r.id = :id")
  Optional<Room> findLockedById(@Param("id") Long id);
}
