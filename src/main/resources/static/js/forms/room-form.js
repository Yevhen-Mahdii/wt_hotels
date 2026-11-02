/** Maps room fields and repeated equipment checkboxes to the typed room API payload. */
import { rest } from "../core/rest.js";
import { state } from "../core/state.js";
import { equipmentOptions } from "../core/catalog.js";
import { fragment, element, setValues } from "../ui/templates.js";
import { openForm } from "../ui/modal.js";
export function roomForm(room, onSaved) {
  const fields = fragment("room-form-template");
  setValues(fields, {
    number: room?.number,
    beds: room?.beds ?? 1,
    price: room?.price ?? 0,
  });
  for (const option of equipmentOptions()) {
    const label = element("equipment-option-template");
    label.querySelector("input").value = option.name;
    label.querySelector("input").checked =
      room?.equipment.includes(option.name) ?? false;
    label.querySelector("span").textContent = option.label;
    fields.querySelector(".checks").append(label);
  }
  openForm(
    room ? "Налаштування номера" : "Новий номер",
    fields,
    (data, formData) =>
      rest.rooms.save(room?.id, {
        ...data,
        hotelId: state.user.hotelId,
        beds: +data.beds,
        price: +data.price,
        equipment: formData.getAll("equipment"),
      }),
    onSaved,
  );
}
