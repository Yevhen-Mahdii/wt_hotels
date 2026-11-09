/** Presents checkout-recorded revenue and totals using the static report table. */
import { startPage } from "../core/page.js";
import { state, hotelName } from "../core/state.js";
import { rest } from "../core/rest.js";
import { money, timestampLabel } from "../core/format.js";
import { element, text } from "../ui/templates.js";
import { bindFilters } from "../ui/filters.js";

async function render(main) {
  const data = await rest.revenue(state.filters);
  text(main, "#total", money(data.total));
  text(main, "#stay-count", data.items.length);
  main.querySelector("#items").replaceChildren(
    ...data.items.map((item) => {
      const row = element("revenue-row-template");
      text(row, "[data-hotel]", hotelName(item.hotelId));
      text(row, "[data-date]", timestampLabel(item.recordedAt));
      text(row, "[data-nights]", item.nights);
      text(row, "[data-amount]", money(item.amount));
      return row;
    }),
  );
  main.querySelector("#revenue-table").hidden = data.items.length === 0;
  main.querySelector("#empty").hidden = data.items.length > 0;
}

startPage(render, (main, { refresh }) => bindFilters(main, refresh));
