/** Centralizes HTTP transport, bearer authentication, API paths and error handling. */
import { session } from "./session.js";
export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}
async function request(path, method = "GET", body) {
  // Public bootstrap/login calls must work even when storage contains an expired or old-key JWT.
  const authenticated = !path.startsWith("/auth/") && path !== "/catalog";
  const response = await fetch("/api" + path, {
    method,
    headers: {
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(authenticated && session.token ? { Authorization: "Bearer " + session.token } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 401 && !path.startsWith("/auth/")) {
      session.clear();
      window.dispatchEvent(new Event("session-expired"));
    }
    throw new ApiError(
      result.message ||
        (response.status === 403
          ? "Недостатньо прав"
          : "Не вдалося виконати запит"),
      response.status,
    );
  }
  return result;
}
function query(values) {
  return (
    "?" +
    new URLSearchParams(
      Object.entries(values).filter(
        ([, value]) => value !== "" && value != null,
      ),
    )
  );
}
/** All HTTP methods and API paths live here. Views only call these named operations. */
export const rest = {
  catalog: () => request("/catalog"),
  auth: {
    status: () => request("/auth/status"),
    me: () => request("/me"),
    login: (data) => request("/auth/login", "POST", data),
    setup: (data) => request("/auth/setup", "POST", data),
  },
  hotels: {
    list: () => request("/hotels"),
    save: (id, data) =>
      request("/hotels" + (id ? "/" + id : ""), id ? "PUT" : "POST", data),
  },
  users: {
    list: () => request("/users"),
    create: (data) => request("/users", "POST", data),
  },
  rooms: {
    list: () => request("/rooms"),
    save: (id, data) =>
      request("/rooms" + (id ? "/" + id : ""), id ? "PUT" : "POST", data),
    search: (filters) => request("/rooms/search" + query(filters)),
    checkIn: (id, bookingId) =>
      request("/rooms/" + id + "/check-in", "POST", { bookingId }),
    checkOut: (id) => request("/rooms/" + id + "/check-out", "POST"),
    completeCleaning: (id) => request("/rooms/" + id + "/clean", "POST"),
  },
  bookings: {
    list: () => request("/bookings"),
    save: (id, data) =>
      request("/bookings" + (id ? "/" + id : ""), id ? "PUT" : "POST", data),
    cancel: (id) => request("/bookings/" + id + "/cancel", "POST"),
  },
  calendar: (filters) => request("/calendar" + query(filters)),
  revenue: (filters) => request("/revenue" + query(filters)),
};
