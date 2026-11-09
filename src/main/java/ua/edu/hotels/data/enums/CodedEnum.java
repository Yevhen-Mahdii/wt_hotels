package ua.edu.hotels.data.enums;

/**
 * Defines stable persistence codes and display labels independently of enum declaration order.
 */
public interface CodedEnum {
  int getCode();

  String getLabel();
}
