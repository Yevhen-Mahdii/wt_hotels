package ua.edu.hotels.service;

import static org.springframework.http.HttpStatus.*;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.repository.*;

/** Publishes enum names, stable codes and labels as the frontend catalog. */
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
