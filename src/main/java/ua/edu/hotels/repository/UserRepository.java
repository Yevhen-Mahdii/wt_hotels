package ua.edu.hotels.repository;

import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import ua.edu.hotels.data.entity.User;

/** Looks up staff accounts by normalized email and provides stable account-list ordering. */
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  List<User> findAllByOrderByEmailAsc();
}
