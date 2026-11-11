/** Populates the shared room HTML template with physical state and reservation information. */
import {equipmentOptions} from "../core/catalog.js";
import {money, today, addDays} from "../core/format.js";
import {badge} from "./components.js";
import {element, text} from "./templates.js";

export function roomCard({room, hotelName, mode, bookings = []}) {
  const card = element("room-card-template");
  text(card, "[data-hotel]", hotelName);
  text(card, "[data-number]", "№ " + room.number);
  text(card, "[data-beds]", room.beds + " ліжок");
  text(card, "[data-price]", money(room.price) + " / ніч");
  card.querySelector("[data-status]").replaceChildren(badge(room.state));

  const currentDate = today();
  const reservations =
    mode === "search"
      ? []
      : bookings
        .filter((booking) => {
          return (
            booking.roomId === room.id &&
            booking.status === "RESERVED" &&
            booking.endDate > currentDate
          );
        })
        .sort((first, second) => first.startDate.localeCompare(second.startDate));
  if (reservations.length) {
    card.querySelector(".room-reservations").hidden = false;
    card.querySelector("[data-booking-status]").replaceChildren(badge("RESERVED"));
    for (const booking of reservations) {
      const dates = element("reservation-dates-template");
      const start = dates.querySelector("[data-start]");
      const end = dates.querySelector("[data-end]");
      start.dateTime = booking.startDate;
      start.textContent =
        booking.startDate === addDays(currentDate, 1) ? "завтра" : booking.startDate;
      end.dateTime = booking.endDate;
      end.textContent = booking.endDate;
      card.querySelector("[data-dates]").append(dates);
    }
  }

  const amenities = card.querySelector(".amenities");
  for (const option of equipmentOptions()) {
    if (!room.equipment.includes(option.name)) {
      continue;
    }
    const node = element("amenity-template");
    node.textContent = option.label;
    amenities.append(node);
  }
  if (!amenities.children.length) {
    amenities.append(element("no-amenities-template"));
  }

  for (const button of card.querySelectorAll(".actions button")) {
    const show = isActionVisible({button, mode, roomState: room.state});
    if (!show) {
      button.remove();
      continue;
    }
    button.dataset.roomId = String(room.id);
  }

  return card;
}

function isActionVisible({button, mode, roomState}) {
  if (mode === "search") {
    return button.hasAttribute("data-book");
  }
  if (button.hasAttribute("data-edit-room")) {
    return true;
  }
  if (button.hasAttribute("data-checkin")) {
    return roomState === "FREE";
  }
  if (button.dataset.action === "check-out") {
    return roomState === "OCCUPIED";
  }
  if (button.dataset.action === "clean") {
    return roomState === "CLEANING";
  }
  return false;
}
