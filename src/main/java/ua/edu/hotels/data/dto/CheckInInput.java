package ua.edu.hotels.data.dto;

import jakarta.validation.constraints.*;

/** Optionally links check-in to a reservation; null means check-in without a prior booking. */
public record CheckInInput(Long bookingId) {}
