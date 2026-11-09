/** Fills the calendar table with all returned states so conflicts remain visible. */
import { startPage } from "../core/page.js";
import { state, hotelName } from "../core/state.js";
import { rest } from "../core/rest.js";
import { today, dateLabel } from "../core/format.js";
import { element, text } from "../ui/templates.js";
import { badge } from "../ui/components.js";
import { bindFilters } from "../ui/filters.js";

async function render(main) {
  const rows = await rest.calendar(state.filters);
  const head = main.querySelector("#calendar-head");
  head.replaceChildren(head.firstElementChild);
  for (const day of rows[0]?.days ?? []) {
    const cell = document.createElement("th");
    cell.scope = "col";
    cell.textContent = dateLabel(day.date);
    cell.classList.toggle("today", day.date === today());
    head.append(cell);
  }

  main.querySelector("#calendar-body").replaceChildren(
    ...rows.map((row) => {
      const tr = element("calendar-row-template");
      text(tr, "[data-number]", "№ " + row.room.number);
      text(tr, "[data-hotel]", hotelName(row.room.hotelId));

      for (const day of row.days) {
        const cell = element("calendar-cell-template");
        cell.classList.toggle("conflict", day.conflict);
        cell.querySelector("[data-states]").replaceChildren(...day.states.map(badge));
        cell.querySelector(".conflict-note").hidden = !day.conflict;
        tr.append(cell);
      }

      return tr;
    }),
  );

  main.querySelector("#calendar").hidden = rows.length === 0;
  main.querySelector("#empty").hidden = rows.length > 0;
}

startPage(render, (main, { refresh }) => {
  main
    .querySelector("#legend")
    .replaceChildren(...["FREE", "RESERVED", "OCCUPIED", "CLEANING"].map(badge));
  bindFilters(main, refresh);
});
