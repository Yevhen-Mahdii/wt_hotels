package ua.edu.hotels.data.dto;

import jakarta.validation.constraints.*;

/**
 * Validates login and setup input; the password service additionally enforces the UTF-8 byte
 * limit.
 */
public record Credentials(
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Size(min = 10, max = 72) String password) {}
