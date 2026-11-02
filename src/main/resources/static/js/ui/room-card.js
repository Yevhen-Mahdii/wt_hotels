/** Shows current physical state and amenities; search availability comes from the booking API. */
import { hotelName } from "../core/state.js";
import { equipmentOptions } from "../core/catalog.js";
import { escapeHtml as esc, money } from "../core/format.js";
import { badge } from "./components.js";
export function roomCard(r, search = false) {
  const amenities = equipmentOptions()
    .filter((option) => r.equipment.includes(option.name))
    .map((option) => '<span class="amenity">' + esc(option.label) + "</span>")
    .join("");
  return (
    '<article class="card"><div class="card-top"><div><div class="eyebrow">' +
    esc(hotelName(r.hotelId)) +
    '</div><div class="room-no">№ ' +
    esc(r.number) +
    "</div></div>" +
    badge(r.state) +
    '</div><div class="room-meta"><span>' +
    r.beds +
    " ліжок</span><strong>" +
    money(r.price) +
    ' / ніч</strong></div><div class="amenities">' +
    (amenities || "<small>Без додаткового оснащення</small>") +
    '</div><div class="actions">' +
    (search
      ? '<button class="small" data-book="' + r.id + '">Забронювати</button>'
      : '<button class="secondary small" data-edit-room="' +
        r.id +
        '">Налаштування</button>' +
        (r.state === "FREE"
          ? '<button class="small" data-checkin="' +
            r.id +
            '">Заселити</button>'
          : r.state === "OCCUPIED"
            ? '<button class="small" data-action="check-out" data-room="' +
              r.id +
              '">Виселити</button>'
            : '<button class="small" data-action="clean" data-room="' +
              r.id +
              '">Клінінг завершено</button>')) +
    "</div></article>"
  );
}
