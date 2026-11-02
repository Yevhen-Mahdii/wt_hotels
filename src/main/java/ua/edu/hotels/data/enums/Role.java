package ua.edu.hotels.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Codes are persistent identifiers. Never reuse or reorder codes in migrations. */
@Getter
@RequiredArgsConstructor
public enum Role implements CodedEnum {
  ADMIN(0, "Адмін"),
  ADMINISTRATOR(1, "Адміністратор"),
  MANAGER(2, "Керуючий");
  private final int code;
  private final String label;

  public static Role fromCode(int code) {
    return java.util.Arrays.stream(values())
        .filter(role -> role.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown role code: " + code));
  }
}
