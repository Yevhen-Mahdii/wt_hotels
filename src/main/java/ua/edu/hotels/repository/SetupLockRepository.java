package ua.edu.hotels.repository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;
import ua.edu.hotels.data.entity.SetupLock;

/**
 * Locks a permanent singleton row so concurrent setup requests cannot create multiple initial
 * admins.
 */
public interface SetupLockRepository extends Repository<SetupLock, Integer> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<SetupLock> findById(Integer id);

  /** Must run inside the setup transaction, before checking whether users exist. */
  default void lock() {
    findById(1).orElseThrow(() -> new EntityNotFoundException("Setup lock row is missing"));
  }
}
