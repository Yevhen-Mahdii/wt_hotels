/** Owns filter inputs and their URL representation; the page owns loading results. */
import {today, addDays} from "../core/format.js";
import {fillHotelSelect} from "./components.js";
import {notify} from "./notifications.js";

export function bindFilters({container, hotels, user, onApply}) {
  const form = container.querySelector("#filters");
  const startInput = form.elements.start;
  const endInput = form.elements.end;
  const bedsInput = form.elements.beds;
  const hotelSelect = form.elements.hotelId;
  const params = new URLSearchParams(location.search);
  const currentDate = today();

  startInput.value = params.get("start") || currentDate;
  endInput.value = params.get("end") || addDays(currentDate, 7);
  if (bedsInput) {
    bedsInput.value = params.get("beds") || "2";
  }
  fillHotelSelect({select: hotelSelect, hotels, selected: params.get("hotelId")});
  if (user.role === "MANAGER") {
    form.querySelector("[data-hotel-filter]").hidden = true;
    hotelSelect.disabled = true;
    hotelSelect.value = "";
  }

  function readValues() {
    const filters = {
      start: startInput.value,
      end: endInput.value,
      hotelId: hotelSelect.value === "" ? null : Number(hotelSelect.value),
    };
    if (bedsInput) {
      filters.beds = bedsInput.valueAsNumber;
    }
    return filters;
  }

  // Refreshing after a mutation must reuse submitted filters, not unfinished input edits.
  let appliedFilters = readValues();

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const filters = readValues();
    appliedFilters = filters;
    const params = new URLSearchParams();
    params.set("start", filters.start);
    params.set("end", filters.end);
    if (filters.hotelId !== null) {
      params.set("hotelId", String(filters.hotelId));
    }
    if (bedsInput) {
      params.set("beds", String(filters.beds));
    }
    history.replaceState(null, "", location.pathname + "?" + params.toString());

    const button = form.querySelector("button");
    button.disabled = true;
    try {
      await onApply();
    } catch (error) {
      notify(error.message, {error: true});
    } finally {
      button.disabled = false;
    }
  });

  function getAppliedValues() {
    return {...appliedFilters};
  }

  return {getAppliedValues};
}
