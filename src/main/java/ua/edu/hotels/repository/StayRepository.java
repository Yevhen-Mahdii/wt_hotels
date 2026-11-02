package ua.edu.hotels.repository;

import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ua.edu.hotels.data.entity.Stay;

/** Loads actual occupancy, including open stays whose checkout timestamp is absent. */
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
