/** Shows assigned-hotel inventory and delegates lifecycle actions to the shared room handlers. */
import { startPage } from "../core/page.js";
import { state, hotelName } from "../core/state.js";
import { roomCard } from "../ui/room-card.js";
import { bindRoomActions } from "../ui/room-actions.js";
import { roomForm } from "../forms/room-form.js";

function render(main) {
  main.querySelector("#page-description").textContent =
    hotelName(state.user.hotelId) + " · щоденна робота готелю";
  main
    .querySelector("#items")
    .replaceChildren(...state.rooms.map((room) => roomCard(room, false, state.bookings)));
  main.querySelector("#empty").hidden = state.rooms.length > 0;
}

startPage(render, (main, { refresh }) => {
  main.querySelector("#add-room").addEventListener("click", () => roomForm(undefined, refresh));
  bindRoomActions(main, refresh);
});
