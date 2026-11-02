package ua.edu.hotels.data.dto;

import java.util.List;
import ua.edu.hotels.data.entity.*;

/** Separates exact bed-count matches from larger rooms that also satisfy the request. */
public record SearchResult(List<Room> exact, List<Room> larger) {}
