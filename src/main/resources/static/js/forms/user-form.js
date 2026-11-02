/** Populates role and hotel choices from shared data; the server enforces assignment rules. */
import { rest } from "../core/rest.js";
import { roleOptions } from "../core/catalog.js";
import { fragment } from "../ui/templates.js";
import { fillHotelSelect } from "../ui/components.js";
import { openForm } from "../ui/modal.js";
export function userForm(onSaved) {
  const fields = fragment("user-form-template");
  fields
    .querySelector('[name="role"]')
    .replaceChildren(
      ...roleOptions().map((role) => new Option(role.label, role.name)),
    );
  fillHotelSelect(fields.querySelector('[name="hotelId"]'));
  openForm(
    "Новий користувач",
    fields,
    (data) =>
      rest.users.create({
        ...data,
        hotelId: data.hotelId ? +data.hotelId : null,
      }),
    onSaved,
  );
}
