/** Displays reservations and offers editing or cancellation only before check-in. */
import { state, hotelName, roomName } from "../core/state.js";
import { rest } from "../core/rest.js";
import { escapeHtml as esc } from "../core/format.js";
import { heading, empty, badge } from "../ui/components.js";
import { notify } from "../ui/notifications.js";
import { bookingForm } from "../forms/booking-form.js";
export async function renderBookings(main, context) {
  main.innerHTML =
    heading("Бронювання", "Дати заїзду та виїзду рахуються від 12:00.") +
    (state.bookings.length
      ? '<div class="grid">' +
        state.bookings
          .map((b) => {
            const room = state.rooms.find((r) => r.id === b.roomId);
            return (
              '<article class="card"><div class="card-top"><h3>№ ' +
              esc(roomName(b.roomId)) +
              "</h3>" +
              badge(b.status) +
              "</div><small>" +
              esc(hotelName(room?.hotelId)) +
              "</small><p>" +
              esc(b.startDate) +
              " → " +
              esc(b.endDate) +
              "</p>" +
              (state.user.role === "ADMINISTRATOR" && b.status === "RESERVED"
                ? '<div class="actions"><button class="secondary small" data-edit-booking="' +
                  b.id +
                  '">Змінити дати</button><button class="danger small" data-cancel="' +
                  b.id +
                  '">Скасувати</button></div>'
                : "") +
              "</article>"
            );
          })
          .join("") +
        "</div>"
      : empty(
          "Бронювань поки немає. Адміністратор створює їх через пошук номерів.",
        ));
  document.querySelectorAll("[data-edit-booking]").forEach(
    (b) =>
      (b.onclick = () =>
        bookingForm(
          state.bookings.find((x) => x.id === +b.dataset.editBooking),
          undefined,
          context.refresh,
        )),
  );
  document.querySelectorAll("[data-cancel]").forEach(
    (b) =>
      (b.onclick = async () => {
        if (!confirm("Скасувати це бронювання?")) return;
        try {
          await rest.bookings.cancel(+b.dataset.cancel);
          notify("Бронювання скасовано");
          await context.refresh();
        } catch (e) {
          notify(e.message, true);
        }
      }),
  );
}
