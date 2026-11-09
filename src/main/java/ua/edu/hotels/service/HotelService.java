package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.data.dto.HotelInput;
import ua.edu.hotels.data.entity.Hotel;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.HotelRepository;
import ua.edu.hotels.security.HotelToken;

import java.util.List;

/**
 * Allows system administrators to maintain hotels and limits manager visibility.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelService {
  private final HotelRepository hotelRepository;

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
    actor.requireRole(Role.ADMIN);
    Hotel hotel = id == null ? new Hotel() : get(id);
    hotel.setName(input.name().trim());
    hotel.setAddress(input.address().trim());
    return hotelRepository.save(hotel);
  }
}
