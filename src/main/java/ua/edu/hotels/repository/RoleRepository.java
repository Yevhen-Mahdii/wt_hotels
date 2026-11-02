package ua.edu.hotels.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.hotels.data.entity.UserRole;

/** Looks up shared role records by the stable enum code rather than a generated identifier. */
public interface RoleRepository extends JpaRepository<UserRole, Integer> {}
