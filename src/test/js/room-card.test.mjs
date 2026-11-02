import assert from "node:assert/strict";
import { test } from "node:test";
import { rest } from "../../main/resources/static/js/core/rest.js";
import { loadCatalog } from "../../main/resources/static/js/core/catalog.js";
import { roomCard } from "../../main/resources/static/js/ui/room-card.js";

rest.catalog = async () => ({
  equipment: [],
  roomStates: [{ name: "FREE", label: "Вільно" }],
  bookingStatuses: [{ name: "RESERVED", label: "Заброньовано" }],
});
await loadCatalog();

const room = { id: 1, hotelId: 1, number: "101", beds: 2, price: 1000, equipment: [], state: "FREE" };
const booking = (overrides = {}) => ({
  roomId: 1, status: "RESERVED", startDate: "2026-11-03", endDate: "2026-11-05", ...overrides,
});
const render = (bookings, search = false, date = "2026-11-02") => roomCard(room, search, bookings, date);

test("shows tomorrow and departure beneath the unchanged physical state", () => {
  const html = render([booking()]);
  assert.ok(html.indexOf("Вільно") < html.indexOf("Заброньовано"));
  assert.match(html, /<time datetime="2026-11-03">завтра<\/time> → <time datetime="2026-11-05">2026-11-05<\/time>/);
  assert.match(html, /data-checkin="1"/);
});

test("shows current and future reservations in date order without mutating the snapshot", () => {
  const bookings = [booking({ startDate: "2026-12-01", endDate: "2026-12-03" }), booking({ startDate: "2026-11-01" })];
  const snapshot = structuredClone(bookings);
  const html = render(bookings);
  assert.ok(html.indexOf('datetime="2026-11-01"') < html.indexOf('datetime="2026-12-01"'));
  assert.doesNotMatch(html, /завтра/);
  assert.deepEqual(bookings, snapshot);
});

test("omits cancelled, checked-in, completed, expired and other rooms' reservations", () => {
  const html = render([
    booking({ status: "CANCELLED" }), booking({ status: "CHECKED_IN" }),
    booking({ status: "COMPLETED" }), booking({ roomId: 2 }),
    booking({ startDate: "2026-11-01", endDate: "2026-11-02" }),
  ]);
  assert.doesNotMatch(html, /Заброньовано|room-booking-dates/);
  assert.match(html, /Вільно/);
});

test("keeps search results and unreserved rooms free of reservation indicators", () => {
  assert.doesNotMatch(render([]), /Заброньовано/);
  const html = render([booking()], true);
  assert.doesNotMatch(html, /Заброньовано|room-booking-dates/);
  assert.match(html, /data-book="1"/);
});

test("recognizes tomorrow across year and leap-day boundaries", () => {
  for (const [date, startDate, endDate] of [
    ["2026-12-31", "2027-01-01", "2027-01-03"],
    ["2028-02-28", "2028-02-29", "2028-03-02"],
  ]) {
    assert.match(render([booking({ startDate, endDate })], false, date), />завтра<\/time>/);
  }
});
