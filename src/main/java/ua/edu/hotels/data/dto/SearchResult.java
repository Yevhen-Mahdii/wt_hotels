package ua.edu.hotels.data.dto;

import ua.edu.hotels.data.entity.Room;

import java.util.List;

/**
 * Separates exact bed-count matches from larger rooms that also satisfy the request.
 */
public record SearchResult(List<Room> exact, List<Room> larger) {
}
