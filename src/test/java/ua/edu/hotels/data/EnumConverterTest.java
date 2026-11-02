package ua.edu.hotels.data;

import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.AttributeConverter;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ua.edu.hotels.data.converter.*;
import ua.edu.hotels.data.enums.*;

/** Checks explicit enum-code round trips and rejection of unknown persisted codes. */
class EnumConverterTest {
  record Case<E extends Enum<E> & CodedEnum>(
      Class<E> type, AttributeConverter<E, Integer> converter) {}

  static Stream<Case<?>> cases() {
    return Stream.of(
        new Case<>(Role.class, new RoleConverter()),
        new Case<>(RoomState.class, new RoomStateConverter()),
        new Case<>(BookingStatus.class, new BookingStatusConverter()),
        new Case<>(Equipment.class, new EquipmentConverter()));
  }

  @ParameterizedTest
  @MethodSource("cases")
  <E extends Enum<E> & CodedEnum> void roundTripsExplicitCodes(Case<E> test) {
    Set<Integer> codes = new HashSet<>();
    for (E value : test.type().getEnumConstants()) {
      assertThat(codes.add(value.getCode())).isTrue();
      assertThat(test.converter().convertToDatabaseColumn(value)).isEqualTo(value.getCode());
      assertThat(test.converter().convertToEntityAttribute(value.getCode())).isEqualTo(value);
    }
  }

  @ParameterizedTest
  @MethodSource("cases")
  void rejectsUnknownDatabaseCode(Case<?> test) {
    assertThatThrownBy(() -> test.converter().convertToEntityAttribute(999))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("cases")
  void preservesNull(Case<?> test) {
    assertThat(test.converter().convertToEntityAttribute(null)).isNull();
    assertThat(test.converter().convertToDatabaseColumn(null)).isNull();
  }
}
