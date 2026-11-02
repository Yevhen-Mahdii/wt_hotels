package ua.edu.hotels.data.converter;

import jakarta.persistence.Converter;
import ua.edu.hotels.data.enums.Equipment;

/** Maps room amenity values to integer codes in the equipment collection table. */
@Converter(autoApply = true)
public class EquipmentConverter extends CodedEnumConverter<Equipment> {
  public EquipmentConverter() {
    super(Equipment.class);
  }
}
