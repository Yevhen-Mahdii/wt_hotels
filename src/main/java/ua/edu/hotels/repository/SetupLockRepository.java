package ua.edu.hotels.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Locks a permanent singleton row so concurrent setup requests cannot create multiple initial
 * admins.
 */
@Repository
@RequiredArgsConstructor
public class SetupLockRepository {
  private final EntityManager entityManager;

  /** Serialize first-run setup across transactions before checking whether users exist. */
  public void lock() {
    entityManager
        .createNativeQuery("SELECT id FROM setup_lock WHERE id = 1 FOR UPDATE")
        .getSingleResult();
  }
}
