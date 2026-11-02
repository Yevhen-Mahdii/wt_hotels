/** Initializes one HTML page: session, shared templates, data and its own page controller. */
import { rest } from "./rest.js";
import { state } from "./state.js";
import { session } from "./session.js";
import { loadCatalog } from "./catalog.js";
import { today, addDays } from "./format.js";
import { allowedPages, homeUrl, loginUrl } from "./routes.js";
import { loadTemplates } from "../ui/templates.js";
import { mountShell } from "../ui/shell.js";
import { mountModal } from "../ui/modal.js";
import { notify } from "../ui/notifications.js";
export async function startPage(render, bind = () => {}) {
  const status = document.querySelector("#page-status");
  const main = document.querySelector("#main");
  const redirectToLogin = () => {
    session.clear();
    location.replace(loginUrl());
  };
  window.addEventListener("session-expired", redirectToLogin);
  // A page restored from the back-forward cache must revalidate its session and data.
  window.addEventListener("pageshow", (event) => {
    if (event.persisted) location.reload();
  });
  try {
    if (!session.token) {
      redirectToLogin();
      return;
    }
    state.user = await rest.auth.me();
    state.page = document.body.dataset.page;
    if (!allowedPages(state.user.role).includes(state.page)) {
      location.replace(homeUrl(state.user.role));
      return;
    }
    await Promise.all([loadCatalog(), loadTemplates()]);
    const params = new URLSearchParams(location.search);
    state.filters = {
      start: params.get("start") || today(),
      end: params.get("end") || addDays(today(), 7),
      beds: params.get("beds") || 2,
      hotelId: params.get("hotelId") || "",
    };
    if (state.user.role === "MANAGER") state.filters.hotelId = "";
    mountShell(() => {
      session.clear();
      location.replace("/index.html");
    });
    mountModal();
    // Refresh data and replace cards after mutations; bind stable page listeners only once below.
    const refresh = async () => {
      [state.hotels, state.rooms, state.bookings] = await Promise.all([
        rest.hotels.list(),
        rest.rooms.list(),
        rest.bookings.list(),
      ]);
      await render(main, { refresh });
    };
    await refresh();
    bind(main, { refresh });
    status.hidden = true;
    document.querySelector(".shell").hidden = false;
  } catch (error) {
    if (error.status === 401) return;
    status.textContent =
      "Не вдалося завантажити сторінку. Оновіть її, щоб повторити спробу.";
    notify(error.message, true);
  }
}
