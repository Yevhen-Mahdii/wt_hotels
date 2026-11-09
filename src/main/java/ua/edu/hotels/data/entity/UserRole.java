package ua.edu.hotels.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.edu.hotels.data.enums.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one enum-backed role shared by many users; its code is the persistent primary key.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_role")
public class UserRole {
  @Id
  private Integer code;

  @Column(nullable = false, unique = true, length = 40)
  private String name;

  @Column(nullable = false, length = 80)
  private String label;

  @JsonIgnore
  @OneToMany(mappedBy = "assignedRole")
  private List<User> users = new ArrayList<>();

  public static UserRole from(Role role) {
    UserRole entity = new UserRole();
    entity.setCode(role.getCode());
    entity.setName(role.name());
    entity.setLabel(role.getLabel());
    return entity;
  }

  @Transient
  public Role getType() {
    return Role.fromCode(code);
  }
}
