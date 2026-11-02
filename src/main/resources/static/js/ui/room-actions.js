/** Binds room actions after rendering and refreshes server data after successful mutations. */
import { rest } from "../core/rest.js";
import { state } from "../core/state.js";
import { roomForm } from "../forms/room-form.js";
import { bookingForm } from "../forms/booking-form.js";
import { checkinForm } from "../forms/checkin-form.js";
import { notify } from "./notifications.js";
export function bindRoomActions(container, onSaved) {
  container.querySelectorAll("[data-edit-room]").forEach(
    (button) =>
      (button.onclick = () =>
        roomForm(
          state.rooms.find((room) => room.id === +button.dataset.editRoom),
          onSaved,
        )),
  );
  container
    .querySelectorAll("[data-book]")
    .forEach(
      (button) =>
        (button.onclick = () =>
          bookingForm(null, +button.dataset.book, onSaved)),
    );
  container
    .querySelectorAll("[data-checkin]")
    .forEach(
      (button) =>
        (button.onclick = () => checkinForm(+button.dataset.checkin, onSaved)),
    );
  container.querySelectorAll("[data-action]").forEach(
    (button) =>
      (button.onclick = async () => {
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
      }),
  );
}
