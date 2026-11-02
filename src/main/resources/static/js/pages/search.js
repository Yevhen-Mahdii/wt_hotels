/** Keeps exact bed-count matches separate from larger available alternatives. */
import { state } from "../core/state.js";
import { rest } from "../core/rest.js";
import { heading, empty } from "../ui/components.js";
import { filterForm, bindFilters } from "../ui/filters.js";
import { roomCard } from "../ui/room-card.js";
import { bindRoomActions } from "../ui/room-actions.js";
export async function renderSearch(main, context) {
  main.innerHTML =
    heading(
      "Знайти номер",
      "Спочатку точний збіг за ліжками, потім просторіші варіанти.",
    ) +
    filterForm(true) +
    '<div id="results" aria-live="polite"></div>';
  bindFilters(main, () => renderSearch(main, context));
  const data = await rest.rooms.search(state.filters);
  document.querySelector("#results").innerHTML =
    [
      ["exact", "Точно за вашим запитом"],
      ["larger", "Є більше ліжок"],
    ]
      .map(
        ([key, title]) =>
          '<h2 class="section-title">' +
          title +
          " <span>" +
          data[key].length +
          " номерів</span></h2>" +
          (data[key].length
            ? '<div class="grid">' +
              data[key].map((r) => roomCard(r, true)).join("") +
              "</div>"
            : empty("На вибрані дати відповідних номерів немає.")),
      )
      .join("") +
    '<p class="help">Показано поточний стан номерів. Майбутні бронювання перевіряються окремо за вибраними датами.</p>';
  bindRoomActions(main, context.refresh);
}
