/** Populates hotel cards and opens editing forms; the page structure belongs to hotels.html. */
import { startPage } from "../core/page.js";
import { state } from "../core/state.js";
import { element, text } from "../ui/templates.js";
import { hotelForm } from "../forms/hotel-form.js";
function render(main) {
  main.querySelector("#items").replaceChildren(
    ...state.hotels.map((hotel) => {
      const card = element("hotel-card-template");
      text(card, "[data-name]", hotel.name);
      text(card, "[data-address]", hotel.address);
      text(
        card,
        "[data-count]",
        state.rooms.filter((r) => r.hotelId === hotel.id).length + " номерів",
      );
      card.querySelector("[data-edit]").dataset.hotel = hotel.id;
      return card;
    }),
  );
  main.querySelector("#empty").hidden = state.hotels.length > 0;
}
startPage(render, (main, { refresh }) => {
  main
    .querySelector("#add-hotel")
    .addEventListener("click", () => hotelForm(undefined, refresh));
  main.querySelector("#items").addEventListener("click", (event) => {
    const button = event.target.closest("[data-hotel]");
    if (button)
      hotelForm(
        state.hotels.find((h) => h.id === +button.dataset.hotel),
        refresh,
      );
  });
});
