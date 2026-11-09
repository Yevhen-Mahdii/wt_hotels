package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ua.edu.hotels.data.dto.UserInput;
import ua.edu.hotels.data.entity.User;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.UserRepository;
import ua.edu.hotels.security.HotelToken;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Creates staff accounts with one role and a hotel assignment only for managers.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
  private final UserRepository userRepository;
  private final HotelService hotelService;
  private final PasswordService passwordService;
  private final RoleService roleService;

  public List<User> list(HotelToken actor) {
    actor.requireRole(Role.ADMIN);
    return userRepository.findAllByOrderByEmailAsc();
  }

  @Transactional
  public User create(HotelToken actor, UserInput input) {
    actor.requireRole(Role.ADMIN);
    if (input.role() == Role.MANAGER) hotelService.get(input.hotelId());
    else if (input.hotelId() != null) {
      log.warn("Staff account creation rejected: only managers can be assigned a hotel; role={}", input.role());
      throw new ResponseStatusException(BAD_REQUEST, "Готель призначається лише Керуючому");
    }
    User user = new User();
    user.setEmail(PasswordService.normalizeEmail(input.email()));
    user.setPasswordHash(passwordService.hash(input.password()));
    user.setAssignedRole(roleService.get(input.role()));
    user.setHotelId(input.hotelId());
    User saved = userRepository.save(user);
    TransactionLog.info(log, "Staff account created: userId={}, role={}, hotelId={}, actorId={}",
      saved.getId(), input.role(), input.hotelId(), actor.getUserId());
    return saved;
  }
}
