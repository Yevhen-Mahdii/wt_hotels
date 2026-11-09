/** Offers reservations valid today or a walk-in; the server validates the selected basis. */
import { rest } from "../core/rest.js";
import { state } from "../core/state.js";
import { money, today } from "../core/format.js";
import { fragment, text } from "../ui/templates.js";
import { openForm } from "../ui/modal.js";

export function checkinForm(id, onSaved) {
  const room = state.rooms.find((r) => r.id === id);
  const currentDate = today();
  const available = state.bookings.filter(
    (b) =>
      b.roomId === id &&
      b.status === "RESERVED" &&
      b.startDate <= currentDate &&
      b.endDate > currentDate,
  );
  const fields = fragment("checkin-form-template");
  text(fields, "[data-price]", money(room.price) + " за ніч");
  fields
    .querySelector("select")
    .replaceChildren(
      ...(available.length
        ? available.map(
            (b) => new Option(`Бронювання #${b.id} · ${b.startDate} → ${b.endDate}`, String(b.id)),
          )
        : [new Option("Без попереднього бронювання", "")]),
    );
  openForm(
    "Заселення · № " + room.number,
    fields,
    (data) => rest.rooms.checkIn(id, data.bookingId ? +data.bookingId : null),
    onSaved,
  );
}
