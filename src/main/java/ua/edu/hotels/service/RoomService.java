package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.RoomInput;
import ua.edu.hotels.data.entity.Room;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.RoomRepository;
import ua.edu.hotels.security.HotelToken;

import java.util.HashSet;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Maintains room details within manager scope and exposes locking for lifecycle operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {
  private final RoomRepository roomRepository;
  private final HotelService hotelService;

  public List<Room> list(HotelToken actor, Long hotelId) {
    Long filter = actor.hotelFilter(hotelId);
    return filter == null
      ? roomRepository.findAllByOrderByHotelIdAscNumberAsc()
      : roomRepository.findByHotelIdOrderByNumberAsc(filter);
  }

  /**
   * Must be called inside the mutation transaction, before reading booking or stay state.
   */
  public Room lock(Long id) {
    return roomRepository.findLockedById(id).orElseThrow(ServiceErrors::notFound);
  }

  @Transactional
  public Room save(HotelToken actor, Long id, RoomInput input) {
    actor.requireRole(Role.MANAGER);
    actor.requireHotelAccess(input.hotelId());
    hotelService.get(input.hotelId());
    Room room = id == null ? new Room() : lock(id);
    if (id != null) {
      actor.requireHotelAccess(room.getHotelId());
      if (!room.getHotelId().equals(input.hotelId())) {
        log.warn("Room update rejected: moving a room between hotels is not allowed; roomId={}", id);
        throw new ResponseStatusException(BAD_REQUEST, "Не можна переносити номер між готелями");
      }
    }
    room.setHotelId(input.hotelId());
    room.setNumber(input.number().trim());
    room.setBeds(input.beds());
    room.setPrice(input.price());
    room.setEquipment(new HashSet<>(input.equipment()));
    return roomRepository.save(room);
  }
}
