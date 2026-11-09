package ua.edu.hotels.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes are persistent identifiers. Never reuse or reorder codes in migrations.
 */
@Getter
@RequiredArgsConstructor
public enum BookingStatus implements CodedEnum {
  RESERVED(0, "Заброньовано"),
  CHECKED_IN(1, "Заселено"),
  COMPLETED(2, "Завершено"),
  CANCELLED(3, "Скасовано");
  private final int code;
  private final String label;
}
