package ua.edu.hotels.data.converter;

import jakarta.persistence.Converter;
import ua.edu.hotels.data.enums.RoomState;

/** Maps physical room states to stable integer codes for persistence. */
@Converter(autoApply = true)
public class RoomStateConverter extends CodedEnumConverter<RoomState> {
  public RoomStateConverter() {
    super(RoomState.class);
  }
}
