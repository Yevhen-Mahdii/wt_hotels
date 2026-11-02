package ua.edu.hotels.repository;

import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ua.edu.hotels.data.entity.Booking;
import ua.edu.hotels.data.enums.BookingStatus;

/** Queries reservation intervals; overlap checks must be paired with a room lock when writing. */
public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByRoomIdInOrderByStartDateDesc(List<Long> ids);

  @Query("select b.roomId from Booking b where b.id = :id")
  Optional<Long> findRoomIdById(@Param("id") Long id);

  @Query(
      """
      select count(b) > 0 from Booking b
      where b.roomId = :roomId and b.status in :statuses
      and b.startDate < :end and b.endDate > :start
      and (:excludedId is null or b.id <> :excludedId)
      """)
  boolean hasOverlap(
      @Param("roomId") Long roomId,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("excludedId") Long excludedId,
      @Param("statuses") List<BookingStatus> statuses);

  @Query(
      """
      select b from Booking b where b.roomId in :roomIds and b.status = :status
      and b.startDate < :end and b.endDate > :start
      """)
  List<Booking> findForCalendar(
      @Param("roomIds") List<Long> roomIds,
      @Param("status") BookingStatus status,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end);
}
