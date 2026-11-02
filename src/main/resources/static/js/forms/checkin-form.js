/** Offers current reservations or an unreserved check-in; the server validates the final choice. */
import { rest } from "../core/rest.js";
import { state, hotelName } from "../core/state.js";
import { escapeHtml as esc, money, today } from "../core/format.js";
import { field } from "../ui/components.js";
import { openForm } from "../ui/modal.js";
export function checkinForm(id, onSaved) {
  const r = state.rooms.find((r) => r.id === id),
    available = state.bookings.filter(
      (b) =>
        b.roomId === id &&
        b.status === "RESERVED" &&
        b.startDate <= today() &&
        b.endDate > today(),
    );
  openForm(
    "Заселення · № " + r.number,
    "<p>Ціна цього проживання: <strong>" +
      money(r.price) +
      ' за ніч</strong>.</p><label>Підстава<select name="bookingId">' +
      (available.length
        ? available
            .map(
              (b) =>
                '<option value="' +
                b.id +
                '">Бронювання #' +
                b.id +
                " · " +
                b.startDate +
                " → " +
                b.endDate +
                "</option>",
            )
            .join("")
        : '<option value="">Без попереднього бронювання</option>') +
      '</select></label><p class="form-note">Дані гостей не збираються. Час заселення фіксується зараз.</p>',
    (data) => rest.rooms.checkIn(id, data.bookingId ? +data.bookingId : null),
    onSaved,
  );
}
