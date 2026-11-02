package ua.edu.hotels.data.converter;

import jakarta.persistence.AttributeConverter;
import java.util.Arrays;
import ua.edu.hotels.data.enums.CodedEnum;

/**
 * Persists explicit enum codes and rejects unknown values instead of silently using enum
 * ordinals.
 */
public abstract class CodedEnumConverter<E extends Enum<E> & CodedEnum>
    implements AttributeConverter<E, Integer> {
  private final Class<E> enumType;

  protected CodedEnumConverter(Class<E> enumType) {
    this.enumType = enumType;
  }

  @Override
  public Integer convertToDatabaseColumn(E value) {
    return value == null ? null : value.getCode();
  }

  @Override
  public E convertToEntityAttribute(Integer code) {
    if (code == null) return null;
    return Arrays.stream(enumType.getEnumConstants())
        .filter(value -> value.getCode() == code)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Unknown " + enumType.getSimpleName() + " code: " + code));
  }
}
