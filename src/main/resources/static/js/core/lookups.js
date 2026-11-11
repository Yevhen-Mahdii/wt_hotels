/** Looks up a display name in the data supplied by the page. */
export function hotelName(hotels, hotelId) {
  const hotel = hotels.find((hotel) => hotel.id === hotelId);
  return hotel?.name ?? "Готель";
}
