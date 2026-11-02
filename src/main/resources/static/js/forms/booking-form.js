/** Edits reservation dates; the server rechecks availability when saving. */
import { rest } from "../core/rest.js";
import { state, hotelName } from "../core/state.js";
import { escapeHtml as esc, money, today } from "../core/format.js";
import { field } from "../ui/components.js";
import { openForm } from "../ui/modal.js";
export function bookingForm(b, roomId, onSaved) {
  roomId = b?.roomId || roomId;
  const r = state.rooms.find((r) => r.id === roomId);
  openForm(
    b ? "Змінити дати" : "Бронювання · № " + r.number,
    '<p class="form-note">' +
      esc(hotelName(r.hotelId)) +
      " · " +
      r.beds +
      " ліжок. Поточна ціна " +
      money(r.price) +
      ", остаточна фіксується при заселенні.</p>" +
      field(
        "Заїзд",
        "startDate",
        "date",
        b?.startDate || state.filters.start,
        'min="' + today() + '"',
      ) +
      field(
        "Виїзд",
        "endDate",
        "date",
        b?.endDate || state.filters.end,
        'min="' + today() + '"',
      ),
    (data) =>
      rest.bookings.save(b?.id, {
        ...data,
        roomId,
      }),
    onSaved,
  );
}
