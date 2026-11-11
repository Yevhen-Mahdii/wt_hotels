/** Loads the selected date range and renders every returned state, including conflicts. */
import {startPage} from "../core/page.js";
import {hotelName} from "../core/lookups.js";
import {rest} from "../core/rest.js";
import {today, dateLabel} from "../core/format.js";
import {element, text} from "../ui/templates.js";
import {badge} from "../ui/components.js";
import {bindFilters} from "../ui/filters.js";

startPage(async ({main, user}) => {
  const hotels = await rest.hotels.list();
  const filters = bindFilters({container: main, hotels, user, onApply: refreshCalendar});
  const head = main.querySelector("#calendar-head");
  const body = main.querySelector("#calendar-body");

  function createCalendarRow(row) {
    const tr = element("calendar-row-template");
    text(tr, "[data-number]", "№ " + row.room.number);
    text(tr, "[data-hotel]", hotelName(hotels, row.room.hotelId));
    for (const day of row.days) {
      const cell = element("calendar-cell-template");
      cell.classList.toggle("conflict", day.conflict);
      cell.querySelector("[data-states]").replaceChildren(...day.states.map(badge));
      cell.querySelector(".conflict-note").hidden = !day.conflict;
      tr.append(cell);
    }
    return tr;
  }

  async function refreshCalendar() {
    const rows = await rest.calendar(filters.getAppliedValues());
    const currentDate = today();
    head.replaceChildren(head.firstElementChild);
    for (const day of rows[0]?.days ?? []) {
      const cell = document.createElement("th");
      cell.scope = "col";
      cell.textContent = dateLabel(day.date);
      cell.classList.toggle("today", day.date === currentDate);
      head.append(cell);
    }
    body.replaceChildren(...rows.map(createCalendarRow));
    main.querySelector("#calendar").hidden = rows.length === 0;
    main.querySelector("#empty").hidden = rows.length > 0;
  }

  const legendStates = ["FREE", "RESERVED", "OCCUPIED", "CLEANING"];
  main.querySelector("#legend").replaceChildren(...legendStates.map(badge));
  await refreshCalendar();
});
