package ua.edu.hotels.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Validates editable hotel details before service-level authorization and persistence.
 */
public record HotelInput(
  @NotBlank @Size(max = 120) String name, @NotBlank @Size(max = 250) String address) {
}
