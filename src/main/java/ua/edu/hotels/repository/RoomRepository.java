package ua.edu.hotels.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ua.edu.hotels.data.entity.Room;

import java.util.List;
import java.util.Optional;

/**
 * Provides the shared room lock used to serialize reservations and occupancy transitions.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {
  List<Room> findAllByOrderByHotelIdAscNumberAsc();

  List<Room> findByHotelIdOrderByNumberAsc(Long hotelId);

  /**
   * Must run inside the transaction that performs the dependent state change.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Room> findLockedById(Long id);
}
