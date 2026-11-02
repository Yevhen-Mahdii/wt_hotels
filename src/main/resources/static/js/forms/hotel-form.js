/** Shares hotel creation and editing through the reusable modal workflow. */
import { rest } from "../core/rest.js";
import { field } from "../ui/components.js";
import { openForm } from "../ui/modal.js";
export function hotelForm(hotel, onSaved) {
  openForm(
    hotel ? "Редагувати готель" : "Новий готель",
    field("Назва", "name", "text", hotel?.name ?? "", 'maxlength="120"') +
      field(
        "Адреса",
        "address",
        "text",
        hotel?.address ?? "",
        'maxlength="250"',
      ),
    (data) => rest.hotels.save(hotel?.id, data),
    onSaved,
  );
}
