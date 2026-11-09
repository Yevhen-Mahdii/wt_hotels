/** Displays reservations and exposes editing or cancellation only for reserved bookings. */
import { startPage } from "../core/page.js";
import { state, hotelName, roomName } from "../core/state.js";
import { rest } from "../core/rest.js";
import { element, text } from "../ui/templates.js";
import { badge } from "../ui/components.js";
import { notify } from "../ui/notifications.js";
import { bookingForm } from "../forms/booking-form.js";

function render(main) {
  main.querySelector("#items").replaceChildren(
    ...state.bookings.map((booking) => {
      const card = element("booking-card-template");
      const room = state.rooms.find((r) => r.id === booking.roomId);
      text(card, "[data-number]", "№ " + roomName(booking.roomId));
      text(card, "[data-hotel]", hotelName(room?.hotelId));
      text(card, "[data-dates]", booking.startDate + " → " + booking.endDate);
      card.querySelector("[data-status]").replaceChildren(badge(booking.status));
      if (state.user.role !== "ADMINISTRATOR" || booking.status !== "RESERVED") {
        card.querySelector(".actions").remove();
      } else {
        card.querySelector("[data-edit]").dataset.id = booking.id;
        card.querySelector("[data-cancel]").dataset.id = booking.id;
      }
      return card;
    }),
  );
  main.querySelector("#empty").hidden = state.bookings.length > 0;
}

startPage(render, (main, { refresh }) => {
  main.querySelector("#items").addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-id]");
    if (!button) {
      return;
    }
    if (button.hasAttribute("data-edit")) {
      bookingForm(
        state.bookings.find((b) => b.id === +button.dataset.id),
        undefined,
        refresh,
      );
    } else if (button.hasAttribute("data-cancel") && confirm("Скасувати це бронювання?")) {
      button.disabled = true;
      try {
        await rest.bookings.cancel(+button.dataset.id);
        notify("Бронювання скасовано");
        await refresh();
      } catch (error) {
        button.disabled = false;
        notify(error.message, true);
      }
    }
  });
});
