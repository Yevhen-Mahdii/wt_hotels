/** Displays checkout-recorded gross revenue, not payments, forecasts or net profit. */
import { state, hotelName } from "../core/state.js";
import { rest } from "../core/rest.js";
import { escapeHtml as esc, money } from "../core/format.js";
import { heading, empty } from "../ui/components.js";
import { filterForm, bindFilters } from "../ui/filters.js";
export async function renderRevenue(main, context) {
  main.innerHTML =
    heading(
      "Валовий дохід",
      "Лише завершені проживання. Без обліку оплат і витрат.",
    ) +
    filterForm() +
    '<div id="revenue"></div>';
  bindFilters(main, () => renderRevenue(main, context));
  const data = await rest.revenue(state.filters);
  document.querySelector("#revenue").innerHTML =
    '<section class="stat"><div class="eyebrow">За вибраний період</div><div class="amount">' +
    money(data.total) +
    "</div><p>" +
    data.items.length +
    " завершених проживань · ціна фіксується при заселенні</p></section>" +
    (data.items.length
      ? '<div class="card table-wrap"><table class="data-table"><thead><tr><th>Готель</th><th>Виселення</th><th>Ночей</th><th>Дохід</th></tr></thead><tbody>' +
        data.items
          .map(
            (r) =>
              "<tr><td>" +
              esc(hotelName(r.hotelId)) +
              "</td><td>" +
              esc(
                new Date(r.recordedAt).toLocaleString("uk-UA", {
                  timeZone: "Europe/Kyiv",
                }),
              ) +
              "</td><td>" +
              r.nights +
              "</td><td>" +
              money(r.amount) +
              "</td></tr>",
          )
          .join("") +
        "</tbody></table></div>"
      : empty("За цей період завершених проживань немає.")) +
    '<p class="help">Період: від 12:00 початкової дати до 12:00 кінцевої (не включно). Дохід записується при виселенні. Мінімальне проживання для розрахунку — одна ніч.</p>';
}
