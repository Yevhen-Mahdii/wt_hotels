package ua.edu.hotels.data.dto;

import jakarta.validation.constraints.*;
import ua.edu.hotels.data.enums.Role;

/** Carries staff account input; hotelId is required for managers and absent for other roles. */
public record UserInput(
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Size(min = 10, max = 72) String password,
    @NotNull Role role,
    Long hotelId) {}
