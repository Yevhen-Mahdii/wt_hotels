/** Binds static filter fields; the URL preserves selections across reload and back navigation. */
import { state } from "../core/state.js";
import { fillHotelSelect } from "./components.js";
import { notify } from "./notifications.js";

export function bindFilters(container, refresh) {
  const form = container.querySelector("#filters");
  form.elements.start.value = state.filters.start;
  form.elements.end.value = state.filters.end;
  if (form.elements.beds) {
    form.elements.beds.value = state.filters.beds;
  }
  fillHotelSelect(form.elements.hotelId, state.filters.hotelId);
  if (state.user.role === "MANAGER") {
    form.querySelector("[data-hotel-filter]").hidden = true;
    form.elements.hotelId.disabled = true;
    state.filters.hotelId = "";
  }
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    state.filters = {
      ...state.filters,
      ...Object.fromEntries(new FormData(form)),
    };
    history.replaceState(null, "", location.pathname + "?" + new URLSearchParams(state.filters));
    const button = form.querySelector("button");
    button.disabled = true;
    try {
      await refresh();
    } catch (error) {
      notify(error.message, true);
    } finally {
      button.disabled = false;
    }
  });
}
