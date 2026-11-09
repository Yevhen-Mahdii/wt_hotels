package ua.edu.hotels.data.dto;

import ua.edu.hotels.data.entity.Room;

import java.util.List;

/**
 * Groups the requested calendar days under their room details.
 */
public record CalendarRow(Room room, List<CalendarDay> days) {
}
