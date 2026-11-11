// Runs real HTML and ES modules in Chromium with an isolated, in-memory API.
// Requires Playwright; PLAYWRIGHT_MODULE and BROWSER_EXECUTABLE may select local installations.
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || "playwright");
const root = path.resolve(__dirname, "../../main/resources/static");
const origin = "http://hotels.test";
const currentDate = new Intl.DateTimeFormat("sv-SE", { timeZone: "Europe/Kyiv" }).format(
  new Date(),
);
const nextDay = new Date(currentDate + "T12:00:00Z");
nextDay.setUTCDate(nextDay.getUTCDate() + 1);
const tomorrow = nextDay.toISOString().slice(0, 10);
const catalog = {
  roles: ["ADMIN", "ADMINISTRATOR", "MANAGER"].map((name) => ({ name, label: name })),
  equipment: ["KETTLE", "TELEVISION"].map((name) => ({ name, label: name })),
  roomStates: ["FREE", "OCCUPIED", "CLEANING"].map((name) => ({ name, label: name })),
  bookingStatuses: ["RESERVED", "CANCELLED", "CHECKED_IN"].map((name) => ({ name, label: name })),
};

async function fixture(browser, role, { loggedIn = true, setup = false } = {}) {
  const context = await browser.newContext();
  const page = await context.newPage();
  page.setDefaultTimeout(5000);
  const errors = [];
  const requests = [];
  const data = {
    user: { id: 1, email: "test@example.com", role, hotelId: role === "MANAGER" ? 1 : null },
    hotels: [{ id: 1, name: "Готель А", address: "Адреса А" }],
    rooms: ["FREE", "OCCUPIED", "CLEANING", "FREE"].map((state, index) => ({
      id: 11 + index,
      hotelId: 1,
      number: String(101 + index),
      beds: index === 3 ? 3 : 2,
      price: 1200.5,
      equipment: ["KETTLE", "TELEVISION"],
      state,
    })),
    bookings: [
      { id: 31, roomId: 11, status: "RESERVED", startDate: currentDate, endDate: tomorrow },
    ],
    users: [],
    failure: null,
    failRefresh: false,
    empty: false,
  };
  page.on("pageerror", (error) => errors.push(error.message));
  page.on("dialog", (dialog) => dialog.accept());
  if (loggedIn) {
    await context.addInitScript(() => {
      if (!sessionStorage.getItem("test-initialized")) {
        sessionStorage.setItem("hotels-token", "test-token");
        sessionStorage.setItem("test-initialized", "yes");
      }
    });
  }
  await page.route("**/*", async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const pathname = url.pathname;
    if (!pathname.startsWith("/api/")) {
      const file = path.join(root, pathname);
      if (!file.startsWith(root + path.sep) || !fs.existsSync(file)) {
        await route.fulfill({ status: 404 });
        return;
      }
      const types = { ".js": "text/javascript", ".html": "text/html", ".css": "text/css" };
      await route.fulfill({ body: fs.readFileSync(file), contentType: types[path.extname(file)] });
      return;
    }

    const method = request.method();
    const body = request.postDataJSON();
    requests.push({
      pathname,
      method,
      body,
      query: Object.fromEntries(url.searchParams),
      headers: request.headers(),
    });
    if (method !== "GET" && data.saveGate) {
      await data.saveGate;
    }
    if (data.failure?.pathname === pathname && data.failure.method === method) {
      const failure = data.failure;
      data.failure = null;
      await route.fulfill({ status: failure.status, json: { message: "Test API failure" } });
      return;
    }
    let result;
    if (pathname === "/api/catalog") result = catalog;
    else if (pathname === "/api/me") result = data.user;
    else if (pathname === "/api/auth/status") result = { setupRequired: setup };
    else if (pathname === "/api/auth/login" || pathname === "/api/auth/setup") {
      setup = false;
      result = { token: "new-token", user: data.user };
    } else if (pathname === "/api/rooms/search") {
      result = {
        exact: data.empty ? [] : [data.rooms[0]],
        larger: data.empty ? [] : [data.rooms[3]],
      };
    } else if (pathname === "/api/calendar") {
      result = data.empty
        ? []
        : [
            {
              room: data.rooms[0],
              days: [{ date: currentDate, states: ["FREE", "RESERVED"], conflict: true }],
            },
          ];
    } else if (pathname === "/api/revenue") {
      result = {
        total: data.empty ? 0 : 2401,
        items: data.empty
          ? []
          : [{ hotelId: 1, recordedAt: currentDate + "T12:00:00Z", nights: 2, amount: 2401 }],
      };
    } else {
      const [, , collection, idText, action] = pathname.split("/");
      const records = data[collection];
      const id = Number(idText);
      const record = records?.find((item) => item.id === id);
      if (method === "GET" && Array.isArray(records)) result = data.empty ? [] : records;
      else if (method === "POST" && !idText && Array.isArray(records)) {
        result = { id: 90, ...body };
        if (collection === "rooms") result.state = "FREE";
        if (collection === "bookings") result.status = "RESERVED";
        records.push(result);
      } else if (method === "PUT" && record) {
        Object.assign(record, body);
        result = record;
      } else if (method === "POST" && record && action) {
        if (action === "check-in") record.state = "OCCUPIED";
        else if (action === "check-out") record.state = "CLEANING";
        else if (action === "clean") record.state = "FREE";
        else if (action === "cancel") record.status = "CANCELLED";
        else throw Error("Unexpected action: " + action);
        result = record;
      }
    }
    assert.notEqual(result, undefined, `Unexpected request: ${method} ${pathname}`);
    if (data.failRefresh && method !== "GET") {
      data.failure = { pathname: "/api/hotels", method: "GET", status: 500 };
      data.failRefresh = false;
    }
    await route.fulfill({ json: result });
  });

  return {
    page,
    data,
    requests,
    async open(name, query = "") {
      await page.goto(`${origin}/pages/${name}.html${query}`);
      await page.locator(".shell").waitFor({ state: "visible" });
    },
    writes() {
      return requests.filter((request) => request.method !== "GET");
    },
    async close() {
      await context.close();
      assert.deepEqual(errors, []);
    },
  };
}

async function submit(page) {
  await page.locator('#modal button[type="submit"]').click();
  await page.locator("#modal").waitFor({ state: "hidden" });
}

async function run() {
  const browser = await chromium.launch({
    headless: true,
    executablePath: process.env.BROWSER_EXECUTABLE,
  });
  try {
    let app = await fixture(browser, "ADMIN");
    await app.open("hotels");
    assert(!app.requests.some((request) => request.pathname === "/api/bookings"));
    await app.page.locator("#add-hotel").click();
    await app.page.locator('#modal [name="name"]').fill("Новий готель");
    await app.page.locator('#modal [name="address"]').fill("Нова адреса");
    await submit(app.page);
    assert.deepEqual(app.writes()[0].body, { name: "Новий готель", address: "Нова адреса" });
    await app.page.locator('#items [data-hotel-id="1"]').click();
    await app.page.locator('#modal [name="name"]').fill("Оновлений готель");
    await submit(app.page);
    assert.equal(app.writes().length, 2, "Reopening must not retain the previous submit handler");
    assert.equal(app.writes()[1].method, "PUT");
    assert.equal(app.writes()[1].pathname, "/api/hotels/1");
    await app.close();
    console.log("PASS hotels: create, update, reopening, page-specific loading");

    app = await fixture(browser, "ADMIN");
    await app.open("users");
    await app.page.locator("#add-user").click();
    await app.page.locator('#modal [name="email"]').fill("manager@example.com");
    await app.page.locator('#modal [name="password"]').fill("test-password");
    await app.page.locator('#modal [name="role"]').selectOption("MANAGER");
    await app.page.locator('#modal [name="hotelId"]').selectOption("1");
    await submit(app.page);
    assert.deepEqual(app.writes()[0].body, {
      email: "manager@example.com",
      password: "test-password",
      role: "MANAGER",
      hotelId: 1,
    });
    assert(!app.requests.some((request) => request.pathname === "/api/rooms"));
    await app.close();
    console.log("PASS users: explicit fields, numeric hotel ID, isolated loading");

    app = await fixture(browser, "MANAGER");
    await app.open("rooms");
    await app.page.locator("#add-room").click();
    await app.page.locator('#modal [name="number"]').fill("201");
    await app.page.locator('#modal [name="beds"]').fill("3");
    await app.page.locator('#modal [name="price"]').fill("-1");
    await app.page.locator('#modal button[type="submit"]').click();
    assert.equal(app.writes().length, 0, "Native validation must reject a negative price");
    await app.page.locator('#modal [name="price"]').fill("0");
    await app.page.locator('#modal [value="KETTLE"]').check();
    await app.page.locator('#modal [value="TELEVISION"]').check();
    await submit(app.page);
    assert.deepEqual(app.writes()[0].body, {
      hotelId: 1,
      number: "201",
      beds: 3,
      price: 0,
      equipment: ["KETTLE", "TELEVISION"],
    });
    await app.page.locator('[data-edit-room][data-room-id="11"]').click();
    await app.page.locator('#modal [name="price"]').fill("1300.75");
    await app.page.locator('#modal [value="KETTLE"]').uncheck();
    await submit(app.page);
    assert.equal(app.writes()[1].pathname, "/api/rooms/11");
    assert.equal(app.writes()[1].body.price, 1300.75);
    assert.deepEqual(app.writes()[1].body.equipment, ["TELEVISION"]);
    await app.page.locator('[data-checkin][data-room-id="11"]').click();
    await submit(app.page);
    assert.deepEqual(app.writes()[2].body, { bookingId: 31 });
    await app.page.locator('[data-action="check-out"][data-room-id="11"]').click();
    await app.page.locator('[data-action="clean"][data-room-id="11"]').waitFor();
    await app.page.locator('[data-action="clean"][data-room-id="11"]').click();
    await app.page.locator('[data-checkin][data-room-id="11"]').waitFor();
    await app.page.locator('[data-checkin][data-room-id="14"]').click();
    await submit(app.page);
    assert.deepEqual(app.writes().at(-1).body, { bookingId: null });
    await app.close();
    console.log("PASS rooms: create, update, equipment, reservation/walk-in, checkout, cleaning");

    app = await fixture(browser, "ADMINISTRATOR");
    await app.open("search", `?start=${currentDate}&end=${tomorrow}&beds=2&hotelId=1`);
    const search = app.requests.find((request) => request.pathname === "/api/rooms/search");
    assert.deepEqual(search.query, { start: currentDate, end: tomorrow, hotelId: "1", beds: "2" });
    assert.equal(await app.page.locator('[data-results="exact"] .card').count(), 1);
    assert.equal(await app.page.locator('[data-results="larger"] .card').count(), 1);
    await app.page.locator('#filters [name="start"]').fill("2099-01-01");
    await app.page.locator('[data-results="larger"] [data-book]').click();
    assert.equal(
      await app.page.locator('#modal [name="startDate"]').inputValue(),
      currentDate,
      "Unsubmitted filters must not change booking dates",
    );
    const refreshedSearch = app.page.waitForResponse((response) =>
      response.url().includes("/api/rooms/search"),
    );
    await submit(app.page);
    assert.deepEqual(app.writes()[0].body, {
      roomId: 14,
      startDate: currentDate,
      endDate: tomorrow,
    });
    await refreshedSearch;
    assert.equal(
      app.requests.filter((request) => request.pathname === "/api/rooms/search").at(-1).query.start,
      currentDate,
      "Refresh must reuse submitted filters",
    );
    assert(
      !app.requests.some(
        (request) => request.pathname === "/api/bookings" && request.method === "GET",
      ),
    );
    await app.close();
    console.log(
      "PASS search: both result groups, URL filters, booking payload and displayed dates",
    );

    app = await fixture(browser, "ADMINISTRATOR");
    await app.open("bookings");
    await app.page.locator("[data-edit][data-booking-id]").click();
    await app.page.locator('#modal [name="startDate"]').fill("2099-01-01");
    await app.page.locator('#modal [name="endDate"]').fill("2099-01-03");
    await submit(app.page);
    assert.deepEqual(app.writes()[0].body, {
      roomId: 11,
      startDate: "2099-01-01",
      endDate: "2099-01-03",
    });
    await app.page.locator("[data-cancel][data-booking-id]").click();
    await app.page.locator("#items .CANCELLED").waitFor();
    assert.equal(app.writes()[1].pathname, "/api/bookings/31/cancel");
    await app.close();
    console.log("PASS bookings: update and cancel");

    for (const name of ["calendar", "revenue"]) {
      app = await fixture(browser, "MANAGER");
      await app.open(name, "?hotelId=999");
      assert.equal(await app.page.locator("[data-hotel-filter]").isVisible(), false);
      const request = app.requests.find((request) => request.pathname === `/api/${name}`);
      assert(!("hotelId" in request.query));
      assert(
        !app.requests.some((request) => ["/api/rooms", "/api/bookings"].includes(request.pathname)),
      );
      if (name === "calendar")
        assert.equal(await app.page.locator("td.conflict .badge").count(), 2);
      else assert.equal(await app.page.locator("#stay-count").textContent(), "1");
      app.data.empty = true;
      await app.page.locator("#filters button").click();
      await app.page.locator("#empty").waitFor({ state: "visible" });
      assert(new URL(app.page.url()).searchParams.has("start"));
      await app.close();
    }
    app = await fixture(browser, "MANAGER");
    await app.open("bookings");
    assert.equal(await app.page.locator("#items .actions").count(), 0);
    await app.close();
    console.log(
      "PASS reports: manager scope, conflicts, totals, filters, empty states, booking permissions",
    );

    app = await fixture(browser, "ADMIN");
    await app.open("hotels");
    await app.page.locator("#items [data-edit]").click();
    app.data.failure = { pathname: "/api/hotels/1", method: "PUT", status: 400 };
    await app.page.locator('#modal button[type="submit"]').click();
    await app.page.locator("#modal .form-error").filter({ hasText: "Test API failure" }).waitFor();
    assert.equal(await app.page.locator('#modal button[type="submit"]').isEnabled(), true);
    app.data.failRefresh = true;
    await submit(app.page);
    await app.page.locator("#notice.error").waitFor();
    assert.equal(await app.page.locator("#modal").isVisible(), false);
    assert.equal(app.writes().length, 2);
    await app.close();
    console.log("PASS errors: failed save stays editable; failed refresh closes a saved form");

    app = await fixture(browser, "ADMIN");
    await app.open("hotels");
    await app.page.locator("#items [data-edit]").click();
    let finishSave;
    app.data.saveGate = new Promise((resolve) => {
      finishSave = resolve;
    });
    await app.page.evaluate(() => {
      const form = document.querySelector("#modal form");
      form.requestSubmit();
      form.requestSubmit();
    });
    assert.equal(await app.page.locator("#close-modal").isDisabled(), true);
    await app.page.keyboard.press("Escape");
    assert.equal(await app.page.locator("#modal").isVisible(), true);
    finishSave();
    await app.page.locator("#modal").waitFor({ state: "hidden" });
    assert.equal(app.writes().length, 1);
    await app.close();
    console.log("PASS pending save: duplicate submit and closing are blocked until completion");

    for (const setup of [false, true]) {
      app = await fixture(browser, "ADMIN", { loggedIn: false, setup });
      await app.page.goto(origin + "/index.html");
      await app.page.locator('#login [name="email"]').fill("admin@example.com");
      await app.page.locator('#login [name="password"]').fill("test-password");
      await app.page.locator("#login button").click();
      await app.page.waitForURL("**/pages/hotels.html");
      await app.page.locator(".shell").waitFor({ state: "visible" });
      assert.equal(app.writes()[0].pathname, setup ? "/api/auth/setup" : "/api/auth/login");
      assert.deepEqual(app.writes()[0].body, {
        email: "admin@example.com",
        password: "test-password",
      });
      assert.equal(app.writes()[0].headers.authorization, undefined);
      assert.equal(
        app.requests.find((request) => request.pathname === "/api/me").headers.authorization,
        "Bearer new-token",
      );
      await app.page.locator("#logout").click();
      await app.page.waitForURL("**/index.html");
      assert.equal(await app.page.evaluate(() => sessionStorage.getItem("hotels-token")), null);
      await app.close();
    }
    app = await fixture(browser, "MANAGER");
    await app.page.goto(origin + "/pages/users.html");
    await app.page.waitForURL("**/pages/rooms.html");
    await app.page.locator(".shell").waitFor({ state: "visible" });
    app.data.failure = { pathname: "/api/me", method: "GET", status: 401 };
    await app.page.reload();
    await app.page.waitForURL("**/index.html?next=**");
    assert.equal(await app.page.evaluate(() => sessionStorage.getItem("hotels-token")), null);
    await app.close();
    console.log(
      "PASS authentication: login, setup, logout, bearer header, role redirect, expired session",
    );
  } finally {
    await browser.close();
  }
}

run().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
