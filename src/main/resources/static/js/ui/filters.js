/** Shares date and hotel filters across views; manager scope is still enforced by the backend. */
import { state } from "../core/state.js";
import { field, hotelSelect } from "./components.js";
import { notify } from "./notifications.js";
export function filterForm(search = false) {
  return (
    '<form id="filters" class="filters">' +
    field("Від", "start", "date", state.filters.start) +
    field("До (виїзд)", "end", "date", state.filters.end) +
    (search
      ? field("Ліжок", "beds", "number", state.filters.beds, 'min="1" max="20"')
      : "") +
    (state.user.role !== "MANAGER"
      ? hotelSelect(true, state.filters.hotelId)
      : "") +
    "<button>" +
    (search ? "Знайти номери" : "Показати") +
    "</button></form>"
  );
}
export function bindFilters(container, render) {
  container.querySelector("#filters").onsubmit = async (event) => {
    event.preventDefault();
    state.filters = {
      ...state.filters,
      ...Object.fromEntries(new FormData(event.target)),
    };
    try {
      await render();
    } catch (error) {
      notify(error.message, true);
    }
  };
}
