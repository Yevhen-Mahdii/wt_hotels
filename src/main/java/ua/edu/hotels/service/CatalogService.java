package ua.edu.hotels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.data.dto.CatalogResponse;
import ua.edu.hotels.data.dto.EnumOption;
import ua.edu.hotels.data.enums.BookingStatus;
import ua.edu.hotels.data.enums.CodedEnum;
import ua.edu.hotels.data.enums.Equipment;
import ua.edu.hotels.data.enums.Role;
import ua.edu.hotels.data.enums.RoomState;

import java.util.Arrays;
import java.util.List;

/**
 * Publishes enum names, stable codes and labels as the frontend catalog.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {
  public CatalogResponse catalog() {
    return new CatalogResponse(
      options(Role.values()),
      options(RoomState.values()),
      options(BookingStatus.values()),
      options(Equipment.values()));
  }

  private <E extends Enum<E> & CodedEnum> List<EnumOption> options(E[] values) {
    return Arrays.stream(values)
      .map(value -> new EnumOption(value.name(), value.getCode(), value.getLabel()))
      .toList();
  }
}
