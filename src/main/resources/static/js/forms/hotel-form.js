/** Fills the hotel form template for creation or editing and submits its values to the API. */
import { rest } from "../core/rest.js";
import { fragment, setValues } from "../ui/templates.js";
import { openForm } from "../ui/modal.js";

export function hotelForm(hotel, onSaved) {
  const fields = fragment("hotel-form-template");
  setValues(fields, { name: hotel?.name, address: hotel?.address });
  openForm(
    hotel ? "Редагувати готель" : "Новий готель",
    fields,
    (data) => rest.hotels.save(hotel?.id, data),
    onSaved,
  );
}
