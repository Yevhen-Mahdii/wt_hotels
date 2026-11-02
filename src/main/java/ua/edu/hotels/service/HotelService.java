package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.*;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.repository.*;
import ua.edu.hotels.security.HotelToken;

/** Allows system administrators to maintain hotels and limits manager visibility. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelService {
  private final HotelRepository hotelRepository;
  private final AccessPolicy accessPolicy;

  public Hotel get(Long id) {
    if (id == null) throw ServiceErrors.notFound();
    return hotelRepository.findById(id).orElseThrow(ServiceErrors::notFound);
  }

  public List<Hotel> list(HotelToken actor) {
    return actor.hasRole(Role.MANAGER)
        ? List.of(get(actor.getHotelId()))
        : hotelRepository.findAllByOrderByNameAsc();
  }

  @Transactional
  public Hotel save(HotelToken actor, Long id, HotelInput input) {
    accessPolicy.requireRole(actor, Role.ADMIN);
    Hotel hotel = id == null ? new Hotel() : get(id);
    hotel.setName(input.name().trim());
    hotel.setAddress(input.address().trim());
    return hotelRepository.save(hotel);
  }
}
