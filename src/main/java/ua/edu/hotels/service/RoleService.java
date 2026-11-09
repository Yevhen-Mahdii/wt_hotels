package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.data.entity.UserRole;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.repository.RoleRepository;

/**
 * Synchronizes the persistent role catalog with stable identifiers defined by the enum.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {
  private final RoleRepository roleRepository;

  public UserRole get(Role role) {
    return roleRepository
      .findById(role.getCode())
      .orElseThrow(() -> new IllegalStateException("Role catalog is not initialized: " + role));
  }

  /**
   * Runs on every start; existing codes remain stable and records are not duplicated.
   */
  @Transactional
  public void initialize() {
    for (Role role : Role.values()) {
      UserRole record =
        roleRepository.findById(role.getCode()).orElseGet(() -> UserRole.from(role));
      record.setName(role.name());
      record.setLabel(role.getLabel());
      roleRepository.save(record);
    }
  }
}
