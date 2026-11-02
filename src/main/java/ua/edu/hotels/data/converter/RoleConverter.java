package ua.edu.hotels.data.converter;

import jakarta.persistence.Converter;
import ua.edu.hotels.data.enums.Role;

/**
 * Converts standalone role values; user role assignments are persisted through the UserRole
 * relationship.
 */
@Converter(autoApply = true)
public class RoleConverter extends CodedEnumConverter<Role> {
  public RoleConverter() {
    super(Role.class);
  }
}
