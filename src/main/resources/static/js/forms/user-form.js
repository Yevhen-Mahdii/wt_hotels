/** Builds role choices from server metadata; hotel assignment is validated again on the server. */
import { rest } from "../core/rest.js";
import { roleOptions } from "../core/catalog.js";
import { escapeHtml as esc } from "../core/format.js";
import { field, hotelSelect } from "../ui/components.js";
import { openForm } from "../ui/modal.js";
export function userForm(onSaved) {
  const roles = roleOptions()
    .map(
      (role) =>
        '<option value="' +
        esc(role.name) +
        '">' +
        esc(role.label) +
        "</option>",
    )
    .join("");
  openForm(
    "Новий користувач",
    field("Email", "email", "email", "", 'maxlength="180" autocomplete="off"') +
      field(
        "Пароль",
        "password",
        "password",
        "",
        'minlength="10" maxlength="72" autocomplete="new-password"',
      ) +
      '<label>Роль<select name="role">' +
      roles +
      "</select></label>" +
      hotelSelect(true) +
      '<p class="form-note">Для Керуючого оберіть один готель. Для інших ролей залиште «Усі готелі».</p>',
    (data) =>
      rest.users.create({
        ...data,
        hotelId: data.hotelId ? +data.hotelId : null,
      }),
    onSaved,
  );
}
