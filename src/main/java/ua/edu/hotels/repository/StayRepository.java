package ua.edu.hotels.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.hotels.data.entity.Stay;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Loads actual occupancy, including open stays whose checkout timestamp is absent.
 */
public interface StayRepository extends JpaRepository<Stay, Long> {
  Optional<Stay> findByRoomIdAndCheckedOutAtIsNull(Long roomId);

  @Query(
    """
      select s from Stay s where s.roomId in :roomIds
      and s.checkedInAt < :end and (s.checkedOutAt is null or s.checkedOutAt >= :start)
      """)
  List<Stay> findForCalendar(
    @Param("roomIds") List<Long> roomIds,
    @Param("start") Instant start,
    @Param("end") Instant end);
}
