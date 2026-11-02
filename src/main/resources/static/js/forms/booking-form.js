/** Fills booking dates and room context; the server rechecks availability on submission. */
import { rest } from "../core/rest.js";
import { state, hotelName } from "../core/state.js";
import { money, today } from "../core/format.js";
import { fragment, text, setValues } from "../ui/templates.js";
import { openForm } from "../ui/modal.js";
export function bookingForm(booking, roomId, onSaved) {
  roomId = booking?.roomId ?? roomId;
  const room = state.rooms.find((r) => r.id === roomId);
  const fields = fragment("booking-form-template");
  text(
    fields,
    "[data-summary]",
    `${hotelName(room.hotelId)} · ${room.beds} ліжок. Поточна ціна ${money(room.price)}, остаточна фіксується при заселенні.`,
  );
  setValues(fields, {
    startDate: booking?.startDate ?? state.filters.start,
    endDate: booking?.endDate ?? state.filters.end,
  });
  fields.querySelectorAll('input[type="date"]').forEach((input) => {
    input.min = today();
  });
  openForm(
    booking ? "Змінити дати" : "Бронювання · № " + room.number,
    fields,
    (data) => rest.bookings.save(booking?.id, { ...data, roomId }),
    onSaved,
  );
}
