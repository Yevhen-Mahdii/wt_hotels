package ua.edu.hotels.data.dto;

import java.util.List;

/**
 * Groups server-owned display metadata for roles, states and room amenities.
 */
public record CatalogResponse(
  List<EnumOption> roles,
  List<EnumOption> roomStates,
  List<EnumOption> bookingStatuses,
  List<EnumOption> equipment) {
}
