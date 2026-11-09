package ua.edu.hotels.data.dto;

import ua.edu.hotels.data.entity.User;

/**
 * Returns a bearer token and the public account details after successful authentication.
 */
public record AuthResponse(String token, User user) {
}
