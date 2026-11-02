package ua.edu.hotels.data.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Carries a room and a half-open reservation range; services validate date ordering and overlap.
 */
public record BookingInput(
    @NotNull Long roomId, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
