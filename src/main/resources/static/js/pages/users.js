/** Displays staff access and opens account creation with server-provided role labels. */
import { rest } from "../core/rest.js";
import { hotelName } from "../core/state.js";
import { roleLabel } from "../core/catalog.js";
import { escapeHtml as esc } from "../core/format.js";
import { heading } from "../ui/components.js";
import { userForm } from "../forms/user-form.js";
export async function renderUsers(main, context) {
  const users = await rest.users.list();
  main.innerHTML =
    heading(
      "Користувачі",
      "Доступи до мережі та окремих готелів.",
      '<button id="add-user">+ Користувач</button>',
    ) +
    '<div class="grid">' +
    users
      .map(
        (u) =>
          '<article class="card"><div class="eyebrow">' +
          esc(roleLabel(u.role)) +
          "</div><h3>" +
          esc(u.email) +
          '</h3><p class="muted">' +
          (u.hotelId ? esc(hotelName(u.hotelId)) : "Уся мережа") +
          "</p></article>",
      )
      .join("") +
    "</div>";
  document.querySelector("#add-user").onclick = () => userForm(context.refresh);
}
