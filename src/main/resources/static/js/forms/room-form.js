/** Converts form strings and repeated amenity fields into the room API payload. */
import { rest } from "../core/rest.js";
import { state } from "../core/state.js";
import { equipmentOptions } from "../core/catalog.js";
import { escapeHtml as esc } from "../core/format.js";
import { field } from "../ui/components.js";
import { openForm } from "../ui/modal.js";
export function roomForm(room, onSaved) {
  const equipment = equipmentOptions()
    .map(
      (option) =>
        '<label><input type="checkbox" name="equipment" value="' +
        esc(option.name) +
        '" ' +
        (room?.equipment.includes(option.name) ? "checked" : "") +
        ">" +
        esc(option.label) +
        "</label>",
    )
    .join("");
  openForm(
    room ? "Налаштування номера" : "Новий номер",
    field(
      "Номер / назва",
      "number",
      "text",
      room?.number ?? "",
      'maxlength="30"',
    ) +
      field(
        "Кількість ліжок",
        "beds",
        "number",
        room?.beds ?? 1,
        'min="1" max="20"',
      ) +
      field(
        "Ціна за ніч, ₴",
        "price",
        "number",
        room?.price ?? 0,
        'min="0" max="99999999.99" step="0.01"',
      ) +
      '<div class="checks">' +
      equipment +
      "</div>",
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
