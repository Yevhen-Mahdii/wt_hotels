package ua.edu.hotels.data.dto;

import jakarta.validation.constraints.*;

/** Validates editable hotel details before service-level authorization and persistence. */
public record HotelInput(
    @NotBlank @Size(max = 120) String name, @NotBlank @Size(max = 250) String address) {}
