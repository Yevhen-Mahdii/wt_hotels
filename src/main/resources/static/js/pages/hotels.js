/** Renders network hotel maintenance using the shared hotel and room snapshot. */
import { state } from "../core/state.js";
import { escapeHtml as esc } from "../core/format.js";
import { heading, empty } from "../ui/components.js";
import { hotelForm } from "../forms/hotel-form.js";
export async function renderHotels(main, context) {
  main.innerHTML =
    heading(
      "Готелі мережі",
      "Створюйте готелі та призначайте Керуючих у розділі користувачів.",
      '<button id="add-hotel">+ Готель</button>',
    ) +
    (state.hotels.length
      ? '<div class="grid">' +
        state.hotels
          .map(
            (h) =>
              '<article class="card"><div class="eyebrow">ГОТЕЛЬ</div><h2>' +
              esc(h.name) +
              '</h2><p class="muted">' +
              esc(h.address) +
              "</p><small>" +
              state.rooms.filter((r) => r.hotelId === h.id).length +
              ' номерів</small><div class="actions"><button class="secondary" data-hotel="' +
              h.id +
              '">Редагувати</button></div></article>',
          )
          .join("") +
        "</div>"
      : empty("Почніть із першого готелю мережі."));
  document.querySelector("#add-hotel").onclick = () =>
    hotelForm(undefined, context.refresh);
  document.querySelectorAll("[data-hotel]").forEach(
    (b) =>
      (b.onclick = () =>
        hotelForm(
          state.hotels.find((h) => h.id === +b.dataset.hotel),
          context.refresh,
        )),
  );
}
