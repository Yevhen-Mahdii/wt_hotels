/** Displays every state returned for a day and highlights conflicts without hiding either state. */
import { state, hotelName } from "../core/state.js";
import { rest } from "../core/rest.js";
import { escapeHtml as esc, today, dateLabel } from "../core/format.js";
import { heading, empty, badge } from "../ui/components.js";
import { filterForm, bindFilters } from "../ui/filters.js";
export async function renderCalendar(main, context) {
  main.innerHTML =
    heading("Розклад номерів", "Бронювання наперед і фактичний стан — поруч.") +
    filterForm() +
    '<div class="legend">' +
    ["FREE", "RESERVED", "OCCUPIED", "CLEANING"].map(badge).join("") +
    '</div><div id="calendar"></div>';
  bindFilters(main, () => renderCalendar(main, context));
  const rows = await rest.calendar(state.filters);
  let html = empty(
    "Номерів ще немає. Керуючий може додати їх у своєму готелі.",
  );
  if (rows.length)
    html =
      '<div class="calendar-scroll" tabindex="0" aria-label="Розклад номерів, прокручуйте горизонтально"><table class="calendar"><thead><tr><th>Номер / готель</th>' +
      rows[0].days
        .map(
          (d) =>
            '<th class="' +
            (d.date === today() ? "today" : "") +
            '">' +
            dateLabel(d.date) +
            "</th>",
        )
        .join("") +
      "</tr></thead><tbody>" +
      rows
        .map(
          (row) =>
            "<tr><td><strong>№ " +
            esc(row.room.number) +
            "</strong><br><small>" +
            esc(hotelName(row.room.hotelId)) +
            "</small></td>" +
            row.days
              .map(
                (d) =>
                  '<td class="' +
                  (d.conflict ? "conflict" : "") +
                  '">' +
                  d.states.map(badge).join("") +
                  (d.conflict
                    ? '<span class="conflict-note">Увага: накладка станів</span>'
                    : "") +
                  "</td>",
              )
              .join("") +
            "</tr>",
        )
        .join("") +
      "</tbody></table></div>";
  document.querySelector("#calendar").innerHTML =
    html +
    '<p class="help">На смартфоні гортайте розклад убік. Помаранчева рамка позначає накладку. Межа доби — 12:00 за Києвом. Майбутній стан «Вільно» означає відсутність бронювання, а не підтвердження прибирання.</p>';
}
