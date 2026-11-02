/** Renders the assigned hotel inventory and binds manager lifecycle actions. */
import { state, hotelName } from "../core/state.js";
import { heading, empty } from "../ui/components.js";
import { roomCard } from "../ui/room-card.js";
import { bindRoomActions } from "../ui/room-actions.js";
import { roomForm } from "../forms/room-form.js";
export async function renderRooms(main, context) {
  main.innerHTML =
    heading(
      "Мої номери",
      hotelName(state.user.hotelId) + " · щоденна робота готелю",
      '<button id="add-room">+ Номер</button>',
    ) +
    (state.rooms.length
      ? '<div class="grid">' +
        state.rooms.map((r) => roomCard(r, false, state.bookings)).join("") +
        "</div>"
      : empty("Додайте номер, зазначте кількість ліжок, оснащення та ціну."));
  document.querySelector("#add-room").onclick = () =>
    roomForm(undefined, context.refresh);
  bindRoomActions(main, context.refresh);
}
