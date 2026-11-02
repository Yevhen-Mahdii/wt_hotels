package ua.edu.hotels.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Codes are persistent identifiers. Never reuse or reorder codes in migrations. */
@Getter
@RequiredArgsConstructor
public enum RoomState implements CodedEnum {
  FREE(0, "Вільно"),
  OCCUPIED(1, "Зайнято"),
  CLEANING(2, "Клінінг");
  private final int code;
  private final String label;
}
