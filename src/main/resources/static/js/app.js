/** Coordinates startup, session recovery, navigation and refreshing shared server data. */
import { rest } from "./core/rest.js";
import { session } from "./core/session.js";
import { state } from "./core/state.js";
import { loadCatalog } from "./core/catalog.js";
import { today, addDays } from "./core/format.js";
import { empty } from "./ui/components.js";
import { notify } from "./ui/notifications.js";
import { closeModal } from "./ui/modal.js";
import { renderShell } from "./ui/shell.js";
import { renderLogin } from "./pages/login.js";
import { renderHotels } from "./pages/hotels.js";
import { renderUsers } from "./pages/users.js";
import { renderRooms } from "./pages/rooms.js";
import { renderSearch } from "./pages/search.js";
import { renderBookings } from "./pages/bookings.js";
import { renderCalendar } from "./pages/calendar.js";
import { renderRevenue } from "./pages/revenue.js";
const container = document.querySelector("#app");
const pages = {
  hotels: renderHotels,
  users: renderUsers,
  rooms: renderRooms,
  search: renderSearch,
  bookings: renderBookings,
  calendar: renderCalendar,
  revenue: renderRevenue,
};
async function renderPage() {
  renderShell(container, navigate, logout);
  await pages[state.page](container.querySelector("#main"), { refresh });
}
async function navigate(page) {
  state.page = page;
  try {
    await renderPage();
  } catch (error) {
    notify(error.message, true);
  }
}
async function refresh() {
  [state.hotels, state.rooms, state.bookings] = await Promise.all([
    rest.hotels.list(),
    rest.rooms.list(),
    rest.bookings.list(),
  ]);
  await renderPage();
}
async function enter() {
  state.page =
    state.user.role === "ADMIN"
      ? "hotels"
      : state.user.role === "ADMINISTRATOR"
        ? "calendar"
        : "rooms";
  state.filters = {
    start: today(),
    end: addDays(today(), 7),
    beds: 2,
    hotelId: "",
  };
  await refresh();
}
function logout() {
  session.clear();
  state.user = null;
  closeModal();
  renderLogin(container, false, enter);
}
async function boot() {
  try {
    await loadCatalog();
    const status = await rest.auth.status();
    if (session.token && !status.setupRequired) {
      try {
        state.user = await rest.auth.me();
      } catch (error) {
        if (error.status !== 401) throw error;
        renderLogin(container, false, enter);
        return;
      }
      await enter();
    } else renderLogin(container, status.setupRequired, enter);
  } catch (error) {
    container.innerHTML = empty(
      "Не вдалося з’єднатися із сервером. Оновіть сторінку.",
    );
    notify(error.message, true);
  }
}
window.addEventListener("session-expired", () => {
  logout();
  notify("Сесія завершилася. Увійдіть знову.", true);
});
boot();
