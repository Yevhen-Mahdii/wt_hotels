/** Chooses navigation by role for usability; hidden navigation is not an authorization boundary. */
import { state } from "../core/state.js";
import { roleLabel } from "../core/catalog.js";
import { escapeHtml as esc } from "../core/format.js";
export function renderShell(container, navigate, onLogout) {
  const tabs =
    state.user.role === "ADMIN"
      ? [
          ["hotels", "Готелі"],
          ["users", "Користувачі"],
        ]
      : state.user.role === "ADMINISTRATOR"
        ? [
            ["calendar", "Розклад"],
            ["search", "Знайти номер"],
            ["bookings", "Бронювання"],
            ["revenue", "Дохід"],
          ]
        : [
            ["rooms", "Мої номери"],
            ["calendar", "Розклад"],
            ["bookings", "Бронювання"],
            ["revenue", "Дохід"],
          ];
  container.innerHTML =
    '<div class="shell"><header class="app-head"><div class="brand"><span class="brand-mark">т</span>Тихий двір</div><div class="account"><span>' +
    esc(state.user.email) +
    "</span><span>" +
    roleLabel(state.user.role) +
    '</span><button id="logout">Вийти</button></div></header><nav class="nav" aria-label="Головне меню"><div class="nav-caption">РОБОЧИЙ ПРОСТІР</div>' +
    tabs
      .map(
        ([id, name]) =>
          '<button data-tab="' +
          id +
          '" class="' +
          (state.page === id ? "active" : "") +
          '" ' +
          (state.page === id ? 'aria-current="page"' : "") +
          ">" +
          name +
          "</button>",
      )
      .join("") +
    '<div class="sidebar-foot">Розрахункова доба<br>12:00 → 12:00<br><br>Часовий пояс: Київ</div></nav><main id="main"></main></div>';
  document.querySelector("#logout").onclick = onLogout;
  container
    .querySelectorAll("[data-tab]")
    .forEach((button) => (button.onclick = () => navigate(button.dataset.tab)));
}
