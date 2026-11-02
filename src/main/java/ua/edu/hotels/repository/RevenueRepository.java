package ua.edu.hotels.repository;

import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ua.edu.hotels.data.entity.Revenue;

/** Selects checkout records in an inclusive-start, exclusive-end reporting interval. */
public interface RevenueRepository extends JpaRepository<Revenue, Long> {
  @Query(
      """
      select r from Revenue r where r.recordedAt >= :start and r.recordedAt < :end
      and (:hotelId is null or r.hotelId = :hotelId) order by r.recordedAt desc
      """)
  List<Revenue> findForPeriod(
      @Param("start") Instant start, @Param("end") Instant end, @Param("hotelId") Long hotelId);
}
