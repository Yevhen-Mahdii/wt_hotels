package ua.edu.hotels.data.converter;

import jakarta.persistence.Converter;
import ua.edu.hotels.data.enums.BookingStatus;

/** Maps booking status values to stable integer codes for persistence. */
@Converter(autoApply = true)
public class BookingStatusConverter extends CodedEnumConverter<BookingStatus> {
  public BookingStatusConverter() {
    super(BookingStatus.class);
  }
}
