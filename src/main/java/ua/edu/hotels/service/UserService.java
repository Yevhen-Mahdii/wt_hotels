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

/** Creates staff accounts with one role and a hotel assignment only for managers. */
@Service
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
    else if (input.hotelId() != null)
      throw new ResponseStatusException(BAD_REQUEST, "Готель призначається лише Керуючому");
    User user = new User();
    user.setEmail(PasswordService.normalizeEmail(input.email()));
    user.setPasswordHash(passwordService.hash(input.password()));
    user.setAssignedRole(roleService.get(input.role()));
    user.setHotelId(input.hotelId());
    return userRepository.save(user);
  }
}
