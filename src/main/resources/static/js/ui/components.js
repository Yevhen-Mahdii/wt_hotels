/** Builds reusable HTML fragments; action markup and extra attributes must be trusted code. */
import { state } from "../core/state.js";
import { escapeHtml as esc } from "../core/format.js";
import { roleLabel, stateLabel } from "../core/catalog.js";
export const badge = (name) =>
  '<span class="badge ' + esc(name) + '">' + esc(stateLabel(name)) + "</span>";
export const empty = (text) => '<div class="empty">' + esc(text) + "</div>";
export function heading(title, text, action = "") {
  return (
    '<div class="page-head"><div><div class="eyebrow">Тихий двір / ' +
    esc(roleLabel(state.user.role)) +
    "</div><h1>" +
    esc(title) +
    "</h1><p>" +
    esc(text) +
    "</p></div>" +
    action +
    "</div>"
  );
}
export function field(label, name, type = "text", value = "", extra = "") {
  return (
    "<label>" +
    esc(label) +
    '<input name="' +
    esc(name) +
    '" type="' +
    esc(type) +
    '" value="' +
    esc(value) +
    '" ' +
    extra +
    " required></label>"
  );
}
export function hotelSelect(all = true, selected = "") {
  const options =
    (all ? '<option value="">Усі готелі</option>' : "") +
    state.hotels
      .map(
        (hotel) =>
          '<option value="' +
          hotel.id +
          '" ' +
          (String(selected) === String(hotel.id) ? "selected" : "") +
          ">" +
          esc(hotel.name) +
          "</option>",
      )
      .join("");
  return (
    '<label>Готель<select name="hotelId" ' +
    (all ? "" : "required") +
    ">" +
    options +
    "</select></label>"
  );
}
