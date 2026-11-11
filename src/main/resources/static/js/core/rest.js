/** Centralizes HTTP transport, bearer authentication, API paths and error handling. */
import {session} from "./session.js";

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

async function request(path, {method = "GET", body, authenticated = true} = {}) {
  const options = {method, headers: {}};
  if (body !== undefined) {
    options.headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  }
  if (authenticated && session.token) {
    options.headers.Authorization = "Bearer " + session.token;
  }

  const response = await fetch("/api" + path, options);
  const result = await response.json().catch(() => ({}));

  if (!response.ok) {
    if (response.status === 401 && authenticated) {
      session.clear();
      window.dispatchEvent(new Event("session-expired"));
    }

    throw new ApiError(
      result.message ||
      (response.status === 403 ? "Недостатньо прав" : "Не вдалося виконати запит"),
      response.status,
    );
  }

  return result;
}

function query(values) {
  const params = new URLSearchParams();
  for (const [name, value] of Object.entries(values)) {
    if (value !== "" && value !== null && value !== undefined) {
      params.set(name, String(value));
    }
  }
  return "?" + params.toString();
}

/** All HTTP methods and API paths live here. Views only call these named operations. */
export const rest = {
  catalog: () => request("/catalog", {authenticated: false}),

  auth: {
    status: () => request("/auth/status", {authenticated: false}),
    me: () => request("/me"),
    login: (data) => request("/auth/login", {method: "POST", body: data, authenticated: false}),
    setup: (data) => request("/auth/setup", {method: "POST", body: data, authenticated: false}),
  },

  hotels: {
    list: () => request("/hotels"),
    create: (data) => request("/hotels", {method: "POST", body: data}),
    update: (id, data) => request(`/hotels/${id}`, {method: "PUT", body: data}),
  },

  users: {
    list: () => request("/users"),
    create: (data) => request("/users", {method: "POST", body: data}),
  },

  rooms: {
    list: () => request("/rooms"),
    create: (data) => request("/rooms", {method: "POST", body: data}),
    update: (id, data) => request(`/rooms/${id}`, {method: "PUT", body: data}),
    search: (filters) => request("/rooms/search" + query(filters)),
    checkIn: (id, bookingId) =>
      request(`/rooms/${id}/check-in`, {
        method: "POST",
        body: {bookingId},
      }),
    checkOut: (id) => request(`/rooms/${id}/check-out`, {method: "POST"}),
    completeCleaning: (id) => request(`/rooms/${id}/clean`, {method: "POST"}),
  },

  bookings: {
    list: () => request("/bookings"),
    create: (data) => request("/bookings", {method: "POST", body: data}),
    update: (id, data) => request(`/bookings/${id}`, {method: "PUT", body: data}),
    cancel: (id) => request(`/bookings/${id}/cancel`, {method: "POST"}),
  },

  calendar: (filters) => request("/calendar" + query(filters)),
  revenue: (filters) => request("/revenue" + query(filters)),
};
