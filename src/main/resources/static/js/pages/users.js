/** Loads staff accounts and fills user-card templates with role and hotel labels. */
import { startPage } from "../core/page.js";
import { rest } from "../core/rest.js";
import { hotelName } from "../core/state.js";
import { roleLabel } from "../core/catalog.js";
import { element, text } from "../ui/templates.js";
import { userForm } from "../forms/user-form.js";

async function render(main) {
  const users = await rest.users.list();
  main.querySelector("#items").replaceChildren(
    ...users.map((user) => {
      const card = element("user-card-template");
      text(card, "[data-role]", roleLabel(user.role));
      text(card, "[data-email]", user.email);
      text(card, "[data-hotel]", user.hotelId ? hotelName(user.hotelId) : "Уся мережа");
      return card;
    }),
  );
}

startPage(render, (main, { refresh }) => {
  main.querySelector("#add-user").addEventListener("click", () => userForm(refresh));
});
