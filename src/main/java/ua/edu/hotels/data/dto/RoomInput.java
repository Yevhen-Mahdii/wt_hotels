package ua.edu.hotels.data.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Carries editable room details; state changes are deliberately handled by separate operations. */
public record RoomInput(
    @NotNull Long hotelId,
    @NotBlank @Size(max = 30) String number,
    @Min(1) @Max(20) int beds,
    @NotNull @DecimalMin("0.00") @DecimalMax("99999999.99") @Digits(integer = 8, fraction = 2)
        BigDecimal price,
    @NotNull java.util.Set<ua.edu.hotels.data.enums.Equipment> equipment) {}
