/**
 * Holds the current client snapshot; refresh it after mutations rather than treating it as
 * authoritative.
 */
export const state = {
  user: null,
  hotels: [],
  rooms: [],
  bookings: [],
  page: "",
  filters: {},
};

export const hotelName = (id) => state.hotels.find((hotel) => hotel.id === id)?.name ?? "Готель";

export const roomName = (id) => state.rooms.find((room) => room.id === id)?.number ?? id;
