package ua.edu.hotels.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Codes are persistent identifiers. Never reuse or reorder codes in migrations. */
@Getter
@RequiredArgsConstructor
public enum Equipment implements CodedEnum {
  KETTLE(0, "Чайник"),
  AIR_CONDITIONER(1, "Кондиціонер"),
  TELEVISION(2, "Телевізор"),
  REFRIGERATOR(3, "Холодильник");
  private final int code;
  private final String label;
}
