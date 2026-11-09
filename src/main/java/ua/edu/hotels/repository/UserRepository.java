package ua.edu.hotels.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.hotels.data.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Looks up staff accounts by normalized email and provides stable account-list ordering.
 */
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  List<User> findAllByOrderByEmailAsc();
}
