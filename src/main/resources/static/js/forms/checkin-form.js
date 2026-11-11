/** Offers today's reservations, or a walk-in when no reservation applies. */
import {money, today} from "../core/format.js";
import {fragment, text} from "../ui/templates.js";

export function createCheckinForm({room, bookings}) {
  const fields = fragment("checkin-form-template");
  const bookingSelect = fields.querySelector('[name="bookingId"]');
  const currentDate = today();
  const availableBookings = bookings.filter((booking) => {
    return (
      booking.roomId === room.id &&
      booking.status === "RESERVED" &&
      booking.startDate <= currentDate &&
      booking.endDate > currentDate
    );
  });

  text(fields, "[data-price]", money(room.price) + " за ніч");
  for (const booking of availableBookings) {
    const label = `Бронювання #${booking.id} · ${booking.startDate} → ${booking.endDate}`;
    bookingSelect.add(new Option(label, String(booking.id)));
  }
  if (availableBookings.length === 0) {
    bookingSelect.add(new Option("Без попереднього бронювання", ""));
  }

  function readValues() {
    const bookingId = bookingSelect.value === "" ? null : Number(bookingSelect.value);
    return {bookingId};
  }

  return {fields, readValues};
}
