/** Small DOM components cloned from shared HTML, never built from HTML strings. */
import {stateLabel} from "../core/catalog.js";
import {element} from "./templates.js";

export function badge(name) {
  const node = element("badge-template");
  node.classList.add(name);
  node.textContent = stateLabel(name);
  return node;
}

export function fillHotelSelect({select, hotels, selected = null}) {
  select.replaceChildren(new Option("Усі готелі", ""));
  for (const hotel of hotels) {
    select.add(new Option(hotel.name, String(hotel.id)));
  }
  select.value = String(selected ?? "");
}
