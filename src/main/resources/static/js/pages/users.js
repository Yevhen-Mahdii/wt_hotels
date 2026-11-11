/** Owns account loading and creation; forms receive the available choices explicitly. */
import {startPage} from "../core/page.js";
import {rest} from "../core/rest.js";
import {hotelName} from "../core/lookups.js";
import {roleLabel, roleOptions} from "../core/catalog.js";
import {element, text} from "../ui/templates.js";
import {createUserForm} from "../forms/user-form.js";
import {openForm} from "../ui/modal.js";

startPage(async ({main}) => {
  let hotels = [];

  async function refreshUsers() {
    const [loadedHotels, users] = await Promise.all([rest.hotels.list(), rest.users.list()]);
    hotels = loadedHotels;
    const cards = users.map((user) => {
      const card = element("user-card-template");
      text(card, "[data-role]", roleLabel(user.role));
      text(card, "[data-email]", user.email);
      text(card, "[data-hotel]", user.hotelId ? hotelName(hotels, user.hotelId) : "Уся мережа");
      return card;
    });
    main.querySelector("#items").replaceChildren(...cards);
  }

  function openUserEditor() {
    const form = createUserForm({hotels, roles: roleOptions()});

    async function createUser() {
      const input = form.readValues();
      await rest.users.create(input);
    }

    openForm({
      title: "Новий користувач",
      fields: form.fields,
      onSubmit: createUser,
      onSaved: refreshUsers,
    });
  }

  main.querySelector("#add-user").addEventListener("click", openUserEditor);
  await refreshUsers();
});
