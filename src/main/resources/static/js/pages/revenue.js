/** Loads checkout-recorded revenue for the selected dates and hotel. */
import {startPage} from "../core/page.js";
import {hotelName} from "../core/lookups.js";
import {rest} from "../core/rest.js";
import {money, timestampLabel} from "../core/format.js";
import {element, text} from "../ui/templates.js";
import {bindFilters} from "../ui/filters.js";

startPage(async ({main, user}) => {
  const hotels = await rest.hotels.list();
  const filters = bindFilters({container: main, hotels, user, onApply: refreshRevenue});

  function createRevenueRow(item) {
    const row = element("revenue-row-template");
    text(row, "[data-hotel]", hotelName(hotels, item.hotelId));
    text(row, "[data-date]", timestampLabel(item.recordedAt));
    text(row, "[data-nights]", item.nights);
    text(row, "[data-amount]", money(item.amount));
    return row;
  }

  async function refreshRevenue() {
    const report = await rest.revenue(filters.getAppliedValues());
    text(main, "#total", money(report.total));
    text(main, "#stay-count", report.items.length);
    main.querySelector("#items").replaceChildren(...report.items.map(createRevenueRow));
    main.querySelector("#revenue-table").hidden = report.items.length === 0;
    main.querySelector("#empty").hidden = report.items.length > 0;
  }

  await refreshRevenue();
});
