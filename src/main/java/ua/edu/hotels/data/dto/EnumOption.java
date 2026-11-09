package ua.edu.hotels.data.dto;

/**
 * Uses name for JSON enum values, code for persistence and label for display.
 */
public record EnumOption(String name, int code, String label) {
}
