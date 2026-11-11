/** Builds booking dates from the room and initial dates supplied by the page. */
import {money, today} from "../core/format.js";
import {fragment, text} from "../ui/templates.js";

export function createBookingForm({room, hotelName, startDate, endDate}) {
  const fields = fragment("booking-form-template");
  const startInput = fields.querySelector('[name="startDate"]');
  const endInput = fields.querySelector('[name="endDate"]');
  text(
    fields,
    "[data-summary]",
    `${hotelName} · ${room.beds} ліжок. Поточна ціна ${money(room.price)}, остаточна фіксується при заселенні.`,
  );
  startInput.value = startDate;
  endInput.value = endDate;
  startInput.min = today();
  endInput.min = today();

  function readValues() {
    return {startDate: startInput.value, endDate: endInput.value};
  }

  return {fields, readValues};
}
