package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.*;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.repository.*;
import ua.edu.hotels.security.HotelToken;

/** Maintains room details within manager scope and exposes locking for lifecycle operations. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {
  private final RoomRepository roomRepository;
  private final HotelService hotelService;
  private final AccessPolicy accessPolicy;

  public List<Room> list(HotelToken actor, Long hotelId) {
    Long filter = accessPolicy.hotelFilter(actor, hotelId);
    return filter == null
        ? roomRepository.findAllByOrderByHotelIdAscNumberAsc()
        : roomRepository.findByHotelIdOrderByNumberAsc(filter);
  }

  /** Must be called inside the mutation transaction, before reading booking or stay state. */
  public Room lock(Long id) {
    return roomRepository.findLockedById(id).orElseThrow(ServiceErrors::notFound);
  }

  @Transactional
  public Room save(HotelToken actor, Long id, RoomInput input) {
    accessPolicy.requireRole(actor, Role.MANAGER);
    accessPolicy.requireHotelAccess(actor, input.hotelId());
    hotelService.get(input.hotelId());
    Room room = id == null ? new Room() : lock(id);
    if (id != null) {
      accessPolicy.requireHotelAccess(actor, room.getHotelId());
      if (!room.getHotelId().equals(input.hotelId()))
        throw new ResponseStatusException(BAD_REQUEST, "Не можна переносити номер між готелями");
    }
    room.setHotelId(input.hotelId());
    room.setNumber(input.number().trim());
    room.setBeds(input.beds());
    room.setPrice(input.price());
    room.setEquipment(new HashSet<>(input.equipment()));
    return roomRepository.save(room);
  }
}
