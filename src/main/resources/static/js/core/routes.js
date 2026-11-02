/** Page visibility improves navigation; the API remains the authorization boundary. */
const pages = {
  ADMIN: ["hotels", "users"],
  ADMINISTRATOR: ["calendar", "search", "bookings", "revenue"],
  MANAGER: ["rooms", "calendar", "bookings", "revenue"],
};
export const allowedPages = (role) => pages[role] ?? [];
export const pageUrl = (page) => `/pages/${page}.html`;
export const homeUrl = (role) => pageUrl(allowedPages(role)[0]);
export function loginUrl() {
  const next = location.pathname + location.search;
  return "/index.html?next=" + encodeURIComponent(next);
}
export function afterLoginUrl(role) {
  const next = new URLSearchParams(location.search).get("next");
  if (next) {
    // Accept return destinations only within the current origin and the user's allowed pages.
    const url = new URL(next, location.origin);
    if (
      url.origin === location.origin &&
      allowedPages(role).some((page) => url.pathname === pageUrl(page))
    ) {
      return url.pathname + url.search;
    }
  }
  return homeUrl(role);
}
