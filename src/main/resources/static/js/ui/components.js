/** Small DOM components cloned from shared HTML, never built from HTML strings. */
import { state } from "../core/state.js";
import { stateLabel } from "../core/catalog.js";
import { element } from "./templates.js";
export function badge(name) {
  const node = element("badge-template");
  node.classList.add(name);
  node.textContent = stateLabel(name);
  return node;
}
export function fillHotelSelect(select, selected = "") {
  select.replaceChildren(
    new Option("Усі готелі", ""),
    ...state.hotels.map((h) => new Option(h.name, String(h.id))),
  );
  select.value = String(selected ?? "");
}
