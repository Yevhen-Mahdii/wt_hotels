package ua.edu.hotels.data.dto;

import java.util.List;
import ua.edu.hotels.data.entity.*;

/** Groups the requested calendar days under their room details. */
public record CalendarRow(Room room, List<CalendarDay> days) {}
