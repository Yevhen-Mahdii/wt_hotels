/** Populates the shared room HTML template with physical state and reservation information. */
import { hotelName } from "../core/state.js";
import { equipmentOptions } from "../core/catalog.js";
import { money, today, addDays } from "../core/format.js";
import { badge } from "./components.js";
import { element, text } from "./templates.js";

export function roomCard(room, search = false, bookings = []) {
  const card = element("room-card-template");
  text(card, "[data-hotel]", hotelName(room.hotelId));
  text(card, "[data-number]", "№ " + room.number);
  text(card, "[data-beds]", room.beds + " ліжок");
  text(card, "[data-price]", money(room.price) + " / ніч");
  card.querySelector("[data-status]").replaceChildren(badge(room.state));

  const currentDate = today();
  const reservations = search
    ? []
    : bookings
        .filter((b) => b.roomId === room.id && b.status === "RESERVED" && b.endDate > currentDate)
        .sort((a, b) => a.startDate.localeCompare(b.startDate));
  if (reservations.length) {
    card.querySelector(".room-reservations").hidden = false;
    card.querySelector("[data-booking-status]").replaceChildren(badge("RESERVED"));
    for (const booking of reservations) {
      const dates = element("reservation-dates-template");
      const start = dates.querySelector("[data-start]"),
        end = dates.querySelector("[data-end]");
      start.dateTime = booking.startDate;
      start.textContent =
        booking.startDate === addDays(currentDate, 1) ? "завтра" : booking.startDate;
      end.dateTime = booking.endDate;
      end.textContent = booking.endDate;
      card.querySelector("[data-dates]").append(dates);
    }
  }

  const amenities = card.querySelector(".amenities");
  for (const option of equipmentOptions().filter((option) =>
    room.equipment.includes(option.name),
  )) {
    const node = element("amenity-template");
    node.textContent = option.label;
    amenities.append(node);
  }
  if (!amenities.children.length) {
    amenities.append(element("no-amenities-template"));
  }

  for (const button of card.querySelectorAll(".actions button")) {
    const show = search
      ? button.hasAttribute("data-book")
      : button.hasAttribute("data-edit-room") ||
        (room.state === "FREE" && button.hasAttribute("data-checkin")) ||
        (room.state === "OCCUPIED" && button.dataset.action === "check-out") ||
        (room.state === "CLEANING" && button.dataset.action === "clean");
    if (!show) {
      button.remove();
      continue;
    }
    for (const attr of ["data-book", "data-edit-room", "data-checkin"]) {
      if (button.hasAttribute(attr)) {
        button.setAttribute(attr, room.id);
      }
    }
    if (button.dataset.action) {
      button.dataset.room = room.id;
    }
  }

  return card;
}
