/** Keeps exact and larger room matches separate while sharing filters and booking actions. */
import { startPage } from "../core/page.js";
import { state } from "../core/state.js";
import { rest } from "../core/rest.js";
import { bindFilters } from "../ui/filters.js";
import { roomCard } from "../ui/room-card.js";
import { bindRoomActions } from "../ui/room-actions.js";

async function render(main) {
  const data = await rest.rooms.search(state.filters);
  for (const key of ["exact", "larger"]) {
    const section = main.querySelector(`[data-results="${key}"]`);
    section.querySelector("[data-count]").textContent = data[key].length + " номерів";
    section.querySelector(".grid").replaceChildren(...data[key].map((r) => roomCard(r, true)));
    section.querySelector(".empty").hidden = data[key].length > 0;
  }
}

startPage(render, (main, { refresh }) => {
  bindFilters(main, refresh);
  bindRoomActions(main, refresh);
});
