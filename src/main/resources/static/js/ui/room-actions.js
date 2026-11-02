/** Delegates room actions from a stable page container so refreshed cards need no rebinding. */
import { rest } from "../core/rest.js";
import { state } from "../core/state.js";
import { roomForm } from "../forms/room-form.js";
import { bookingForm } from "../forms/booking-form.js";
import { checkinForm } from "../forms/checkin-form.js";
import { notify } from "./notifications.js";
export function bindRoomActions(container, onSaved) {
  container.addEventListener("click", async (event) => {
    const button = event.target.closest("button");
    if (!button || !container.contains(button)) return;
    if (button.hasAttribute("data-edit-room"))
      roomForm(
        state.rooms.find((r) => r.id === +button.dataset.editRoom),
        onSaved,
      );
    else if (button.hasAttribute("data-book"))
      bookingForm(null, +button.dataset.book, onSaved);
    else if (button.hasAttribute("data-checkin"))
      checkinForm(+button.dataset.checkin, onSaved);
    else if (button.dataset.action) {
      if (
        !confirm(
          button.dataset.action === "check-out"
            ? "Завершити проживання та зафіксувати дохід?"
            : "Підтвердити, що номер прибраний і готовий?",
        )
      )
        return;
      button.disabled = true;
      try {
        await (
          button.dataset.action === "check-out"
            ? rest.rooms.checkOut
            : rest.rooms.completeCleaning
        )(+button.dataset.room);
        notify("Стан номера оновлено");
        await onSaved();
      } catch (error) {
        button.disabled = false;
        notify(error.message, true);
      }
    }
  });
}
